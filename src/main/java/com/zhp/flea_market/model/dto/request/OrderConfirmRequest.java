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
     * 确认说明（可选）
     */
    private String description;
}