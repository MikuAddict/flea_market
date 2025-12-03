package com.zhp.flea_market.model.dto.request;

import lombok.Data;

/**
 * 订单确认请求
 */
@Data
public class OrderConfirmRequest {
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 确认类型 (1-买家确认收货, 2-卖家确认收款)
     */
    private Integer confirmType;
    
    /**
     * 确认说明（可选）
     */
    private String description;
}