package com.zhp.flea_market.model.dto.request;

import lombok.Data;

/**
 * 支付凭证请求
 */
@Data
public class PaymentProofRequest {
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 支付凭证URL
     */
    private String proofUrl;
    
    /**
     * 支付凭证说明（可选）
     */
    private String description;
}