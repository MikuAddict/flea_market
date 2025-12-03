package com.zhp.flea_market.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhp.flea_market.model.entity.Order;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     *
     * @param productId 商品ID
     * @param paymentMethod 支付方式
     * @param request HTTP请求
     * @return 创建的订单ID
     */
    Long createOrder(Long productId, Integer paymentMethod, HttpServletRequest request);

    /**
     * 支付订单
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否支付成功
     */
    boolean payOrder(Long orderId, HttpServletRequest request);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否取消成功
     */
    boolean cancelOrder(Long orderId, HttpServletRequest request);

    /**
     * 完成订单
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否完成成功
     */
    boolean completeOrder(Long orderId, HttpServletRequest request);

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 订单详情
     */
    Order getOrderDetail(Long orderId, HttpServletRequest request);

    /**
     * 获取买家订单列表
     *
     * @param request HTTP请求
     * @param page 分页参数
     * @return 订单列表
     */
    List<Order> getBuyerOrders(HttpServletRequest request, Page<Order> page);

    /**
     * 获取卖家订单列表
     *
     * @param request HTTP请求
     * @param page 分页参数
     * @return 订单列表
     */
    List<Order> getSellerOrders(HttpServletRequest request, Page<Order> page);

    /**
     * 根据状态获取买家订单列表
     *
     * @param status 订单状态
     * @param request HTTP请求
     * @param page 分页参数
     * @return 订单列表
     */
    List<Order> getBuyerOrdersByStatus(Integer status, HttpServletRequest request, Page<Order> page);

    /**
     * 根据状态获取卖家订单列表
     *
     * @param status 订单状态
     * @param request HTTP请求
     * @param page 分页参数
     * @return 订单列表
     */
    List<Order> getSellerOrdersByStatus(Integer status, HttpServletRequest request, Page<Order> page);

    /**
     * 获取订单统计信息
     *
     * @param request HTTP请求
     * @return 订单统计信息
     */
    OrderStatistics getOrderStatistics(HttpServletRequest request);

    /**
     * 获取所有订单（管理员权限）
     *
     * @param page 分页参数
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> getAllOrders(Page<Order> page, Integer status);

    /**
     * 获取订单查询条件
     *
     * @param buyerId 买家ID
     * @param sellerId 卖家ID
     * @param status 订单状态
     * @return 查询条件
     */
    QueryWrapper<Order> getQueryWrapper(Long buyerId, Long sellerId, Integer status);

    /**
     * 验证订单权限
     *
     * @param order 订单
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean validateOrderPermission(Order order, Long userId);

    /**
     * 计算订单金额
     *
     * @param productId 商品ID
     * @return 订单金额
     */
    BigDecimal calculateOrderAmount(Long productId);

    /**
     * 订单统计信息类
     */
    class OrderStatistics {
        private int totalOrders;
        private int pendingPaymentOrders;
        private int paidOrders;
        private int completedOrders;
        private int cancelledOrders;
        private BigDecimal totalAmount;

        // 构造函数、getter和setter
        public OrderStatistics() {}

        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

        public int getPendingPaymentOrders() { return pendingPaymentOrders; }
        public void setPendingPaymentOrders(int pendingPaymentOrders) { this.pendingPaymentOrders = pendingPaymentOrders; }

        public int getPaidOrders() { return paidOrders; }
        public void setPaidOrders(int paidOrders) { this.paidOrders = paidOrders; }

        public int getCompletedOrders() { return completedOrders; }
        public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }

        public int getCancelledOrders() { return cancelledOrders; }
        public void setCancelledOrders(int cancelledOrders) { this.cancelledOrders = cancelledOrders; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }
}