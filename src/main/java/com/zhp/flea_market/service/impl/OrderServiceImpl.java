package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.mapper.OrderMapper;
import com.zhp.flea_market.model.dto.request.OrderConfirmRequest;
import com.zhp.flea_market.model.dto.request.OrderRequest;
import com.zhp.flea_market.model.entity.Order;
import com.zhp.flea_market.model.entity.Product;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.model.vo.OrderVO;
import com.zhp.flea_market.service.OrderService;
import com.zhp.flea_market.service.ProductService;
import com.zhp.flea_market.service.TradeRecordService;
import com.zhp.flea_market.service.UserService;
import com.zhp.flea_market.utils.PageUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;
    
    @Autowired
    @Lazy
    private TradeRecordService tradeRecordService;

    /**
     * 创建订单
     * @param productId 二手物品ID
     * @param request HTTP请求
     * @return 创建的订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public Long createOrder(Long productId, HttpServletRequest request) {
        // 参数校验
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品ID无效");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 检查二手物品是否存在且已上架
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "二手物品不存在");
        }
        
        if (product.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品未上架，无法购买");
        }
        
        // 自动使用二手物品设置的支付方式
        Integer paymentMethod = product.getPaymentMethod();
        if (paymentMethod == null || paymentMethod < 0 || paymentMethod > 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品支付方式设置无效");
        }
        
        // 检查不能购买自己的二手物品
        if (product.getUserId() != null && product.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能购买自己的二手物品");
        }
        
        // 计算订单金额
        BigDecimal amount = calculateOrderAmount(productId);
        
        // 如果是积分兑换，检查用户积分是否足够
        if (paymentMethod == 2) { // 2表示积分兑换
            BigDecimal userPoints = userService.getUserPoints(currentUser.getId());
            if (userPoints == null || userPoints.compareTo(amount) < 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "积分不足，无法兑换");
            }
            // 扣除用户积分
            boolean pointsDeducted = userService.updateUserPoints(currentUser.getId(), amount.negate());
            if (!pointsDeducted) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "积分扣除失败");
            }
        }
        
        // 创建订单
        Order order = new Order();
        order.setProductId(productId);
        order.setBuyerId(currentUser.getId());
        order.setSellerId(product.getUserId());
        order.setAmount(amount);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(1);
        order.setBuyerConfirmed(false);
        order.setCreateTime(new Date());
        
        boolean saved = this.save(order);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单创建失败");
        }
        
        // 将商品状态设置为3（已售出）
        Product updateProduct = new Product();
        updateProduct.setId(productId);
        updateProduct.setStatus(3); // 已售出
        productService.updateById(updateProduct);
        
        // 如果是积分兑换，创建积分记录
        if (paymentMethod == 2) {
            try {
                // 创建交易记录
                tradeRecordService.createTradeRecord(
                        order.getId(),
                        product.getId(),
                        product.getProductName(),
                        order.getBuyerId(),
                        currentUser.getUserName(),
                        order.getSellerId(),
                        userService.getById(order.getSellerId()).getUserName(),
                        order.getAmount(),
                        order.getPaymentMethod(),
                        "积分兑换",
                        "订单创建时自动使用积分兑换"
                );
            } catch (Exception e) {
                // 交易记录创建失败不影响订单状态，但记录日志
                System.err.println("积分兑换交易记录创建失败: " + e.getMessage());
            }
        }
        
        return order.getId();
    }

    /**
     * 支付订单
     * @param orderId 订单ID
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
        if (validateOrderPermission(order, currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限支付该订单");
        }
        
        return true;
    }

    /**
     * 取消订单
     * @param orderId 订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
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
        if (validateOrderPermission(order, currentUser.getId())) {
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
        
        boolean updated = this.updateById(updateOrder);
        
        // 如果订单支付方式是积分兑换，返还积分
        if (updated && order.getPaymentMethod() == 2) {
            try {
                // 返还积分给买家
                userService.updateUserPoints(order.getBuyerId(), order.getAmount());
            } catch (Exception e) {
                System.err.println("取消订单时积分返还失败: " + e.getMessage());
            }
        }
        
        // 恢复商品状态为已上架
        try {
            Product updateProduct = new Product();
            updateProduct.setId(order.getProductId());
            updateProduct.setStatus(1);
            productService.updateById(updateProduct);
        } catch (Exception e) {
            System.err.println("取消订单时恢复商品状态失败: " + e.getMessage());
        }
        
        return updated;
    }

    /**
     * 买家确认收货（完成订单）
     * @param orderId 订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 60)
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
        
        // 权限校验：只有买家可以确认收货
        if (order.getBuyerId() == null || !order.getBuyerId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有买家可以确认收货");
        }
        
        // 检查订单状态
        if (order.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单未支付，无法确认收货");
        }
        
        // 检查是否已经确认过
        if (order.getBuyerConfirmed() != null && order.getBuyerConfirmed()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单已经确认收货");
        }
        
        // 更新订单状态为已完成
        Order updateOrder = new Order();
        updateOrder.setId(orderId);
        updateOrder.setBuyerConfirmed(true);
        updateOrder.setStatus(2); // 已完成
        updateOrder.setFinishTime(new Date());
        
        boolean updated = this.updateById(updateOrder);
        
        // 订单完成后，给买家和卖家各加100积分，并创建交易记录，同时将二手物品标记为已售出
        if (updated) {
            try {
                // 买家加100积分
                userService.updateUserPoints(order.getBuyerId(), new BigDecimal("100"));
                // 卖家加100积分
                userService.updateUserPoints(order.getSellerId(), new BigDecimal("100"));
                
                // 自动将二手物品标记为已售出
                productService.markProductAsSold(order.getProductId());
                
                // 创建交易记录
                Product product = productService.getById(order.getProductId());
                User buyer = userService.getById(order.getBuyerId());
                User seller = userService.getById(order.getSellerId());
                String paymentMethodDesc = getPaymentMethodDesc(order.getPaymentMethod());
                
                tradeRecordService.createTradeRecord(
                        orderId,
                        product.getId(),
                        product.getProductName(),
                        order.getBuyerId(),
                        buyer.getUserName(),
                        order.getSellerId(),
                        seller.getUserName(),
                        order.getAmount(),
                        order.getPaymentMethod(),
                        paymentMethodDesc,
                        "订单完成自动创建交易记录"
                );
            } catch (Exception e) {
                // 积分更新、交易记录创建和二手物品状态更新失败不影响订单完成状态，但记录日志
                System.err.println("订单完成时积分更新、交易记录创建或二手物品状态更新失败: " + e.getMessage());
            }
        }
        
        return updated;
    }

    /**
     * 获取订单详情
     * @param orderId 订单ID
     */
    @Override
    public OrderVO getOrderDetail(Long orderId, HttpServletRequest request) {
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
        if (validateOrderPermission(order, currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该订单");
        }
        
        // 转换为VO对象

        return convertToOrderVO(order);
    }

    /**
     * 获取买家订单列表
     * @param page 分页参数
     */
    @Override
    public List<Order> getBuyerOrders(HttpServletRequest request, Page<Order> page) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        return PageUtils.getPageResult(this, page, queryWrapper -> {
            queryWrapper.eq("buyer_id", currentUser.getId());
            queryWrapper.orderByDesc("create_time");
        });
    }

    /**
     * 获取卖家订单列表
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
        
        return PageUtils.getPageResult(this, page, queryWrapper -> {
            queryWrapper.eq("seller_id", currentUser.getId());
            queryWrapper.orderByDesc("create_time");
        });
    }

    /**
     * 获取订单统计信息
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
     * @param buyerId 买家ID
     * @param sellerId 卖家ID
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
     * @param order 订单
     * @param userId 用户ID
     */
    @Override
    public boolean validateOrderPermission(Order order, Long userId) {
        return !order.getBuyerId().equals(userId) &&
                !order.getSellerId().equals(userId);
    }

    /**
     * 计算订单金额
     * @param productId 二手物品ID
     * @return 订单金额
     */
    @Override
    public BigDecimal calculateOrderAmount(Long productId) {
        Product product = productService.getById(productId);
        if (product == null || product.getPrice() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品价格信息错误");
        }
        
        return product.getPrice();
    }

    /**
     * 确认订单（买家确认收货）
     * @param confirmRequest 订单确认请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 60)
    public boolean confirmOrder(OrderConfirmRequest confirmRequest, HttpServletRequest request) {
        // 参数校验
        if (confirmRequest == null || confirmRequest.getOrderId() == null || confirmRequest.getOrderId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID无效");
        }
        
        // 确认类型只能是买家确认收货（1）
        if (confirmRequest.getConfirmType() == null || confirmRequest.getConfirmType() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "确认类型无效，只能进行买家确认收货");
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
        
        // 权限校验：只有买家可以确认收货
        if (order.getBuyerId() == null || !order.getBuyerId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有买家可以确认收货");
        }
        
        // 检查订单状态
        if (order.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单状态异常，无法确认");
        }
        
        // 检查是否已经确认过
        if (order.getBuyerConfirmed() != null && order.getBuyerConfirmed()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单已经确认收货");
        }
        
        // 更新订单确认状态
        Order updateOrder = new Order();
        updateOrder.setId(confirmRequest.getOrderId());
        
        // 买家确认收货，订单直接完成
        updateOrder.setBuyerConfirmed(true);
        updateOrder.setStatus(2); // 已完成
        updateOrder.setFinishTime(new Date());
        
        boolean updated = this.updateById(updateOrder);
        
        // 订单完成后，创建交易记录并添加积分
        if (updated) {
            try {
                // 积分支付订单不发放积分
                if (order.getPaymentMethod() != 2) { // 2表示积分兑换
                    BigDecimal pointsToAdd = order.getAmount().divide(new BigDecimal("10"), 1, RoundingMode.HALF_UP);
                    userService.updateUserPoints(order.getBuyerId(), pointsToAdd);
                }
                
                // 创建交易记录
                Product product = productService.getById(order.getProductId());
                User buyer = userService.getById(order.getBuyerId());
                User seller = userService.getById(order.getSellerId());
                String paymentMethodDesc = getPaymentMethodDesc(order.getPaymentMethod());
                
                tradeRecordService.createTradeRecord(
                        order.getId(),
                        product.getId(),
                        product.getProductName(),
                        order.getBuyerId(),
                        buyer.getUserName(),
                        order.getSellerId(),
                        seller.getUserName(),
                        order.getAmount(),
                        order.getPaymentMethod(),
                        paymentMethodDesc,
                        "买家确认收货自动创建交易记录"
                );
            } catch (Exception e) {
                // 交易记录创建失败不影响订单完成状态，但记录日志
                System.err.println("订单完成时交易记录创建失败: " + e.getMessage());
            }
        }
        
        return updated;
    }

    /**
     * 获取支付方式描述
     *
     * @param paymentMethod 支付方式
     * @return 支付方式描述
     */
    private String getPaymentMethodDesc(Integer paymentMethod) {
        return switch (paymentMethod) {
            case 0 -> "现金支付";
            case 1 -> "微信支付";
            case 2 -> "积分兑换";
            case 3 -> "物品交换";
            default -> "未知支付方式";
        };
    }

    /**
     * 将订单实体转换为VO对象
     *
     * @param order 订单实体
     * @return 订单VO对象
     */
    private OrderVO convertToOrderVO(Order order) {
        OrderVO orderVO = new OrderVO();
        
        // 复制基本属性
        orderVO.setId(order.getId());
        orderVO.setProductId(order.getProductId());
        orderVO.setBuyerId(order.getBuyerId());
        orderVO.setSellerId(order.getSellerId());
        orderVO.setAmount(order.getAmount());
        orderVO.setPaymentMethod(order.getPaymentMethod());
        orderVO.setStatus(order.getStatus());
        orderVO.setStatusDesc(getOrderStatusDesc(order.getStatus()));
        orderVO.setBuyerConfirmed(order.getBuyerConfirmed());
        orderVO.setCreateTime(order.getCreateTime());
        orderVO.setFinishTime(order.getFinishTime());
        
        // 获取二手物品信息
        Product product = productService.getById(order.getProductId());
        if (product != null) {
            orderVO.setProductName(product.getProductName());
            orderVO.setProductImage(product.getMainImageUrl());
        }
        
        // 获取买家信息
        User buyer = userService.getById(order.getBuyerId());
        if (buyer != null) {
            orderVO.setBuyerName(buyer.getUserName());
        }
        
        // 获取卖家信息
        User seller = userService.getById(order.getSellerId());
        if (seller != null) {
            orderVO.setSellerName(seller.getUserName());
        }
        
        // 设置支付方式描述
        orderVO.setPaymentMethodDesc(getPaymentMethodDesc(order.getPaymentMethod()));
        
        return orderVO;
    }

    /**
     * 获取订单状态描述
     *
     * @param status 订单状态
     * @return 状态描述
     */
    private String getOrderStatusDesc(Integer status) {
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "已支付";
            case 2 -> "已完成";
            case 3 -> "已取消";
            default -> "未知状态";
        };
    }
}