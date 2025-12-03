package com.zhp.flea_market.model.dto.request;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 订单统计请求
 */
@Data
public class OrderRequest {
    
    /**
     * 总订单数
     */
    private int totalOrders;
    
    /**
     * 待支付订单数
     */
    private int pendingPaymentOrders;
    
    /**
     * 已支付订单数
     */
    private int paidOrders;
    
    /**
     * 已完成订单数
     */
    private int completedOrders;
    
    /**
     * 已取消订单数
     */
    private int cancelledOrders;
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
}