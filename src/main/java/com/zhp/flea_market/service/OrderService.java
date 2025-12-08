package com.zhp.flea_market.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhp.flea_market.model.entity.Order;
import com.zhp.flea_market.model.vo.OrderVO;
import com.zhp.flea_market.model.dto.request.OrderRequest;
import com.zhp.flea_market.model.dto.request.PaymentProofRequest;
import com.zhp.flea_market.model.dto.request.OrderConfirmRequest;
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
     * @param productId 二手物品ID
     * @param request HTTP请求
     * @return 创建的订单ID
     */
    Long createOrder(Long productId, HttpServletRequest request);

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
    OrderVO getOrderDetail(Long orderId, HttpServletRequest request);

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
    OrderRequest getOrderStatistics(HttpServletRequest request);

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
     * @param productId 二手物品ID
     * @return 订单金额
     */
    BigDecimal calculateOrderAmount(Long productId);

    /**
     * 提交支付凭证
     *
     * @param proofRequest 支付凭证请求
     * @param request HTTP请求
     * @return 是否提交成功
     */
    boolean submitPaymentProof(PaymentProofRequest proofRequest, HttpServletRequest request);

    /**
     * 确认订单（买家确认收货或卖家确认收款）
     *
     * @param confirmRequest 订单确认请求
     * @param request HTTP请求
     * @return 是否确认成功
     */
    boolean confirmOrder(OrderConfirmRequest confirmRequest, HttpServletRequest request);

    /**
     * 模拟微信支付
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否支付成功
     */
    boolean simulateWechatPay(Long orderId, HttpServletRequest request);

    /**
     * 使用积分兑换二手物品
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否兑换成功
     */
    boolean exchangeWithPoints(Long orderId, HttpServletRequest request);

    /**
     * 申请物品交换
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否申请成功
     */
    boolean applyForExchange(Long orderId, HttpServletRequest request);

    /**
     * 确认物品交换
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否确认成功
     */
    boolean confirmExchange(Long orderId, HttpServletRequest request);



}