package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.mapper.OrderMapper;
import com.zhp.flea_market.model.dto.request.OrderRequest;
import com.zhp.flea_market.model.dto.request.PaymentProofRequest;
import com.zhp.flea_market.model.dto.request.OrderConfirmRequest;
import com.zhp.flea_market.model.entity.Order;
import com.zhp.flea_market.model.entity.Product;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.service.OrderService;
import com.zhp.flea_market.service.ProductService;
import com.zhp.flea_market.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
        
        // 验证商品是否支持该支付方式
        if (!validatePaymentMethod(productId, paymentMethod)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该商品不支持此支付方式");
        }
        
        // 如果是积分兑换，检查商品是否允许积分购买
        if (paymentMethod == 2 && (product.getAllowPointsPurchase() == null || !product.getAllowPointsPurchase())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该商品不支持积分兑换");
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
        
        boolean updated = this.updateById(updateOrder);
        
        // 订单完成后，给买家和卖家各加100积分
        if (updated) {
            try {
                // 买家加100积分
                userService.updateUserPoints(order.getBuyer().getId(), 100);
                // 卖家加100积分
                userService.updateUserPoints(order.getSeller().getId(), 100);
            } catch (Exception e) {
                // 积分更新失败不影响订单完成状态，但记录日志
                System.err.println("订单完成时积分更新失败: " + e.getMessage());
            }
        }
        
        return updated;
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
    public OrderRequest getOrderStatistics(HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        OrderRequest statistics = new OrderRequest();
        
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

    /**
     * 提交支付凭证
     *
     * @param proofRequest 支付凭证请求
     * @param request HTTP请求
     * @return 是否提交成功
     */
    @Override
    public boolean submitPaymentProof(PaymentProofRequest proofRequest, HttpServletRequest request) {
        // 参数校验
        if (proofRequest == null || proofRequest.getOrderId() == null || proofRequest.getOrderId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID无效");
        }
        
        if (proofRequest.getProofUrl() == null || proofRequest.getProofUrl().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付凭证不能为空");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 检查订单是否存在
        Order order = this.getById(proofRequest.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }
        
        // 权限校验：只有买家可以提交支付凭证
        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该订单");
        }
        
        // 检查订单支付方式
        if (order.getPaymentMethod() != 0) { // 0表示现金支付
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只有现金支付需要提交支付凭证");
        }
        
        // 检查订单状态
        if (order.getStatus() != 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单状态异常，无法提交凭证");
        }
        
        // 更新订单支付凭证
        Order updateOrder = new Order();
        updateOrder.setId(proofRequest.getOrderId());
        updateOrder.setPaymentProof(proofRequest.getProofUrl());
        updateOrder.setStatus(1); // 更新为已支付状态，等待卖家确认
        
        return this.updateById(updateOrder);
    }

    /**
     * 确认订单（买家确认收货或卖家确认收款）
     *
     * @param confirmRequest 订单确认请求
     * @param request HTTP请求
     * @return 是否确认成功
     */
    @Override
    public boolean confirmOrder(OrderConfirmRequest confirmRequest, HttpServletRequest request) {
        // 参数校验
        if (confirmRequest == null || confirmRequest.getOrderId() == null || confirmRequest.getOrderId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID无效");
        }
        
        if (confirmRequest.getConfirmType() == null || confirmRequest.getConfirmType() < 1 || confirmRequest.getConfirmType() > 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "确认类型无效");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 检查订单是否存在
        Order order = this.getById(confirmRequest.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }
        
        // 根据确认类型进行权限校验
        if (confirmRequest.getConfirmType() == 1 && !order.getBuyer().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有买家可以确认收货");
        }
        
        if (confirmRequest.getConfirmType() == 2 && !order.getSeller().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有卖家可以确认收款");
        }
        
        // 检查订单状态
        if (order.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单状态异常，无法确认");
        }
        
        // 更新订单确认状态
        Order updateOrder = new Order();
        updateOrder.setId(confirmRequest.getOrderId());
        
        if (confirmRequest.getConfirmType() == 1) {
            // 买家确认收货
            updateOrder.setBuyerConfirmed(true);
            
            // 如果是微信支付或积分兑换，买家确认后直接完成订单
            if (order.getPaymentMethod() == 1 || order.getPaymentMethod() == 2) {
                updateOrder.setStatus(2); // 已完成
                updateOrder.setFinishTime(new Date());
                
                // 订单完成后，给买家和卖家各加100积分
                try {
                    userService.updateUserPoints(order.getBuyer().getId(), 100);
                    userService.updateUserPoints(order.getSeller().getId(), 100);
                } catch (Exception e) {
                    System.err.println("订单完成时积分更新失败: " + e.getMessage());
                }
            }
        } else if (confirmRequest.getConfirmType() == 2) {
            // 卖家确认收款
            updateOrder.setSellerConfirmed(true);
            
            // 如果是现金支付，卖家确认后直接完成订单
            if (order.getPaymentMethod() == 0) {
                updateOrder.setStatus(2); // 已完成
                updateOrder.setFinishTime(new Date());
                
                // 订单完成后，给买家和卖家各加100积分
                try {
                    userService.updateUserPoints(order.getBuyer().getId(), 100);
                    userService.updateUserPoints(order.getSeller().getId(), 100);
                } catch (Exception e) {
                    System.err.println("订单完成时积分更新失败: " + e.getMessage());
                }
            }
        }
        
        return this.updateById(updateOrder);
    }

    /**
     * 模拟微信支付
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否支付成功
     */
    @Override
    public boolean simulateWechatPay(Long orderId, HttpServletRequest request) {
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
        
        // 权限校验：只有买家可以支付
        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该订单");
        }
        
        // 检查订单支付方式
        if (order.getPaymentMethod() != 1) { // 1表示微信支付
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单支付方式不是微信支付");
        }
        
        // 检查订单状态
        if (order.getStatus() != 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单状态异常，无法支付");
        }
        
        // 模拟微信支付过程（在实际项目中，这里会调用微信支付API）
        try {
            // 模拟支付延迟
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "支付过程被中断");
        }
        
        // 更新订单状态为已支付
        Order updateOrder = new Order();
        updateOrder.setId(orderId);
        updateOrder.setStatus(1); // 已支付
        
        return this.updateById(updateOrder);
    }

    /**
     * 使用积分兑换商品
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否兑换成功
     */
    @Override
    public boolean exchangeWithPoints(Long orderId, HttpServletRequest request) {
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
        
        // 权限校验：只有买家可以支付
        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该订单");
        }
        
        // 检查订单支付方式
        if (order.getPaymentMethod() != 2) { // 2表示积分兑换
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单支付方式不是积分兑换");
        }
        
        // 检查订单状态
        if (order.getStatus() != 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单状态异常，无法兑换");
        }
        
        // 检查商品是否允许积分购买
        Product product = productService.getById(order.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在");
        }
        
        if (product.getAllowPointsPurchase() == null || !product.getAllowPointsPurchase()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该商品不支持积分兑换");
        }
        
        // 检查用户积分是否足够（假设商品价格1元=1积分）
        Integer userPoints = userService.getUserPoints(currentUser.getId());
        if (userPoints == null || userPoints < order.getAmount().intValue()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "积分不足，无法兑换");
        }
        
        // 扣除用户积分
        boolean pointsDeducted = userService.updateUserPoints(currentUser.getId(), -order.getAmount().intValue());
        if (!pointsDeducted) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "积分扣除失败");
        }
        
        // 更新订单状态为已支付
        Order updateOrder = new Order();
        updateOrder.setId(orderId);
        updateOrder.setStatus(1); // 已支付
        
        return this.updateById(updateOrder);
    }

    /**
     * 申请物品交换
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否申请成功
     */
    @Override
    public boolean applyForExchange(Long orderId, HttpServletRequest request) {
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
        
        // 权限校验：只有买家可以申请交换
        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该订单");
        }
        
        // 检查订单支付方式
        if (order.getPaymentMethod() != 3) { // 3表示物品交换
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单支付方式不是物品交换");
        }
        
        // 检查订单状态
        if (order.getStatus() != 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单状态异常，无法申请交换");
        }
        
        // 更新订单状态为等待卖家确认
        Order updateOrder = new Order();
        updateOrder.setId(orderId);
        updateOrder.setStatus(1); // 已支付，等待卖家确认
        
        return this.updateById(updateOrder);
    }

    /**
     * 确认物品交换
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否确认成功
     */
    @Override
    public boolean confirmExchange(Long orderId, HttpServletRequest request) {
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
        
        // 权限校验：只有卖家可以确认交换
        if (!order.getSeller().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该订单");
        }
        
        // 检查订单支付方式
        if (order.getPaymentMethod() != 3) { // 3表示物品交换
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单支付方式不是物品交换");
        }
        
        // 检查订单状态
        if (order.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单状态异常，无法确认交换");
        }
        
        // 更新订单状态为已完成
        Order updateOrder = new Order();
        updateOrder.setId(orderId);
        updateOrder.setStatus(2); // 已完成
        updateOrder.setFinishTime(new Date());
        
        boolean updated = this.updateById(updateOrder);
        
        // 订单完成后，给买家和卖家各加100积分
        if (updated) {
            try {
                userService.updateUserPoints(order.getBuyer().getId(), 100);
                userService.updateUserPoints(order.getSeller().getId(), 100);
            } catch (Exception e) {
                System.err.println("订单完成时积分更新失败: " + e.getMessage());
            }
        }
        
        return updated;
    }

    /**
     * 验证商品是否支持指定的支付方式
     *
     * @param productId 商品ID
     * @param paymentMethod 支付方式
     * @return 是否支持
     */
    @Override
    public boolean validatePaymentMethod(Long productId, Integer paymentMethod) {
        if (productId == null || productId <= 0 || paymentMethod == null || paymentMethod < 0 || paymentMethod > 3) {
            return false;
        }
        
        Product product = productService.getById(productId);
        if (product == null || product.getPaymentOptions() == null) {
            return false;
        }
        
        // 检查支付方式是否在商品支持的支付方式中
        return (product.getPaymentOptions() & (1 << paymentMethod)) != 0;
    }
}