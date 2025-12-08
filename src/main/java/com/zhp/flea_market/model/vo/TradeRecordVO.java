package com.zhp.flea_market.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 交易记录视图对象
 */
@Data
public class TradeRecordVO {
    /**
     * 交易记录ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 买家名称
     */
    private String buyerName;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 卖家名称
     */
    private String sellerName;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 支付方式描述
     */
    private String paymentMethodDesc;

    /**
     * 交易时间
     */
    private Date tradeTime;

    /**
     * 交易状态 (1-成功, 2-已完成评价, 3-已退款)
     */
    private Integer tradeStatus;

    /**
     * 状态描述
     */
    private String tradeStatusDesc;

    /**
     * 交易备注
     */
    private String remark;
}