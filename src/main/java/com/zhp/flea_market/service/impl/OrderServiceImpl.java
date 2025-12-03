package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.mapper.OrderMapper;
import com.zhp.flea_market.model.entity.Order;
import com.zhp.flea_market.model.entity.Product;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.service.OrderService;
import com.zhp.flea_market.service.ProductService;
import com.zhp.flea_market.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    /**
     * 创建订单
     *
     * @param productId 商品ID
     * @param paymentMethod 支付方式
     * @param request HTTP请求
     * @return 创建的订单ID
     */
    @Override
    public Long createOrder(Long productId, Integer paymentMethod, HttpServletRequest request) {
        // 参数校验
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID无效");
        }
        
        if (paymentMethod == null || paymentMethod < 0 || paymentMethod > 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付方式无效");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 检查商品是否存在且已上架
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在");
        }
        
        if (product.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品未上架，无法购买");
        }
        
        // 检查不能购买自己的商品
        if (product.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能购买自己的商品");
        }
        
        // 计算订单金额
        BigDecimal amount = calculateOrderAmount(productId);
        
        // 创建订单
        Order order = new Order();
        order.setProductId(productId);
        order.setBuyer(currentUser);
        order.setSeller(product.getUser());
        order.setAmount(amount);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(0); // 待支付
        order.setCreateTime(new Date());
        
        boolean saved = this.save(order);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单创建失败");
        }
        
        return order.getId();
    }

    /**
     * 支付订单
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否支付成功
     */
    @Override
    public boolean payOrder(Long orderId, HttpServletRequest request) {
        // 参数校验
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID无效");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 检查订单是否存在
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }
        
        // 权限校验：只能支付自己的订单
        if (!validateOrderPermission(order, currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限支付该订单");
        }
        
        // 检查订单状态
        if (order.getStatus() != 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单状态异常，无法支付");
        }
        
        // 检查商品是否仍然有效
        Product product = productService.getById(order.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品已下架或不存在，无法支付");
        }
        
        // 更新订单状态为已支付
        Order updateOrder = new Order();
        updateOrder.setId(orderId);
        updateOrder.setStatus(1); // 已支付
        
        return this.updateById(updateOrder);
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否取消成功
     */
    @Override
    public boolean cancelOrder(Long orderId, HttpServletRequest request) {
        // 参数校验
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID无效");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 检查订单是否存在
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }
        
        // 权限校验：只能取消自己的订单
        if (!validateOrderPermission(order, currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限取消该订单");
        }
        
        // 检查订单状态
        if (order.getStatus() == 2 || order.getStatus() == 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单已完成或已取消，无法再次取消");
        }
        
        // 更新订单状态为已取消
        Order updateOrder = new Order();
        updateOrder.setId(orderId);
        updateOrder.setStatus(3); // 已取消
        
        return this.updateById(updateOrder);
    }

    /**
     * 完成订单
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否完成成功
     */
    @Override
    public boolean completeOrder(Long orderId, HttpServletRequest request) {
        // 参数校验
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID无效");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 检查订单是否存在
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }
        
        // 权限校验：卖家或买家可以完成订单
        if (!order.getBuyer().getId().equals(currentUser.getId()) && 
            !order.getSeller().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限完成该订单");
        }
        
        // 检查订单状态
        if (order.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单未支付，无法完成");
        }
        
        // 更新订单状态为已完成
        Order updateOrder = new Order();
        updateOrder.setId(orderId);
        updateOrder.setStatus(2); // 已完成
        updateOrder.setFinishTime(new Date());
        
        return this.updateById(updateOrder);
    }

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 订单详情
     */
    @Override
    public Order getOrderDetail(Long orderId, HttpServletRequest request) {
        // 参数校验
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID无效");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 检查订单是否存在
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }
        
        // 权限校验：只能查看自己的订单
        if (!validateOrderPermission(order, currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该订单");
        }
        
        return order;
    }

    /**
     * 获取买家订单列表
     *
     * @param request HTTP请求
     * @param page 分页参数
     * @return 订单列表
     */
    @Override
    public List<Order> getBuyerOrders(HttpServletRequest request, Page<Order> page) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("buyer_id", currentUser.getId());
        queryWrapper.orderByDesc("create_time");
        
        Page<Order> resultPage = this.page(page, queryWrapper);
        return resultPage.getRecords();
    }

    /**
     * 获取卖家订单列表
     *
     * @param request HTTP请求
     * @param page 分页参数
     * @return 订单列表
     */
    @Override
    public List<Order> getSellerOrders(HttpServletRequest request, Page<Order> page) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("seller_id", currentUser.getId());
        queryWrapper.orderByDesc("create_time");
        
        Page<Order> resultPage = this.page(page, queryWrapper);
        return resultPage.getRecords();
    }

    /**
     * 根据状态获取买家订单列表
     *
     * @param status 订单状态
     * @param request HTTP请求
     * @param page 分页参数
     * @return 订单列表
     */
    @Override
    public List<Order> getBuyerOrdersByStatus(Integer status, HttpServletRequest request, Page<Order> page) {
        // 参数校验
        if (status == null || status < 0 || status > 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单状态无效");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("buyer_id", currentUser.getId());
        queryWrapper.eq("status", status);
        queryWrapper.orderByDesc("create_time");
        
        Page<Order> resultPage = this.page(page, queryWrapper);
        return resultPage.getRecords();
    }

    /**
     * 根据状态获取卖家订单列表
     *
     * @param status 订单状态
     * @param request HTTP请求
     * @param page 分页参数
     * @return 订单列表
     */
    @Override
    public List<Order> getSellerOrdersByStatus(Integer status, HttpServletRequest request, Page<Order> page) {
        // 参数校验
        if (status == null || status < 0 || status > 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单状态无效");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("seller_id", currentUser.getId());
        queryWrapper.eq("status", status);
        queryWrapper.orderByDesc("create_time");
        
        Page<Order> resultPage = this.page(page, queryWrapper);
        return resultPage.getRecords();
    }

    /**
     * 获取订单统计信息
     *
     * @param request HTTP请求
     * @return 订单统计信息
     */
    @Override
    public OrderStatistics getOrderStatistics(HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        OrderStatistics statistics = new OrderStatistics();
        
        // 获取买家订单统计
        QueryWrapper<Order> buyerWrapper = new QueryWrapper<>();
        buyerWrapper.eq("buyer_id", currentUser.getId());
        statistics.setTotalOrders((int) this.count(buyerWrapper));
        
        // 各状态订单数量
        buyerWrapper.eq("status", 0);
        statistics.setPendingPaymentOrders((int) this.count(buyerWrapper));
        
        buyerWrapper = new QueryWrapper<>();
        buyerWrapper.eq("buyer_id", currentUser.getId()).eq("status", 1);
        statistics.setPaidOrders((int) this.count(buyerWrapper));
        
        buyerWrapper = new QueryWrapper<>();
        buyerWrapper.eq("buyer_id", currentUser.getId()).eq("status", 2);
        statistics.setCompletedOrders((int) this.count(buyerWrapper));
        
        buyerWrapper = new QueryWrapper<>();
        buyerWrapper.eq("buyer_id", currentUser.getId()).eq("status", 3);
        statistics.setCancelledOrders((int) this.count(buyerWrapper));
        
        // 计算总金额（已完成订单）
        buyerWrapper = new QueryWrapper<>();
        buyerWrapper.eq("buyer_id", currentUser.getId()).eq("status", 2);
        List<Order> completedOrders = this.list(buyerWrapper);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Order order : completedOrders) {
            if (order.getAmount() != null) {
                totalAmount = totalAmount.add(order.getAmount());
            }
        }
        statistics.setTotalAmount(totalAmount);
        
        return statistics;
    }

    /**
     * 获取所有订单（管理员权限）
     *
     * @param page 分页参数
     * @param status 订单状态
     * @return 订单列表
     */
    @Override
    public List<Order> getAllOrders(Page<Order> page, Integer status) {
        QueryWrapper<Order> queryWrapper = getQueryWrapper(null, null, status);
        
        Page<Order> resultPage = this.page(page, queryWrapper);
        return resultPage.getRecords();
    }

    /**
     * 获取订单查询条件
     *
     * @param buyerId 买家ID
     * @param sellerId 卖家ID
     * @param status 订单状态
     * @return 查询条件
     */
    @Override
    public QueryWrapper<Order> getQueryWrapper(Long buyerId, Long sellerId, Integer status) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        
        if (buyerId != null && buyerId > 0) {
            queryWrapper.eq("buyer_id", buyerId);
        }
        
        if (sellerId != null && sellerId > 0) {
            queryWrapper.eq("seller_id", sellerId);
        }
        
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByDesc("create_time");
        
        return queryWrapper;
    }

    /**
     * 验证订单权限
     *
     * @param order 订单
     * @param userId 用户ID
     * @return 是否有权限
     */
    @Override
    public boolean validateOrderPermission(Order order, Long userId) {
        return order.getBuyer().getId().equals(userId) || 
               order.getSeller().getId().equals(userId);
    }

    /**
     * 计算订单金额
     *
     * @param productId 商品ID
     * @return 订单金额
     */
    @Override
    public BigDecimal calculateOrderAmount(Long productId) {
        Product product = productService.getById(productId);
        if (product == null || product.getPrice() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品价格信息错误");
        }
        
        return product.getPrice();
    }
}