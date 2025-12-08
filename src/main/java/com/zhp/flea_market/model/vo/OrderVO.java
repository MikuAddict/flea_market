package com.zhp.flea_market.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单视图对象
 */
@Data
public class OrderVO {
    /**
     * 订单ID
     */
    private Long id;

    /**
     * 二手物品ID
     */
    private Long productId;

    /**
     * 二手物品名称
     */
    private String productName;

    /**
     * 二手物品图片
     */
    private String productImage;

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
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 支付方式 (0-现金, 1-微信, 2-积分兑换, 3-物品交换)
     */
    private Integer paymentMethod;

    /**
     * 支付方式描述
     */
    private String paymentMethodDesc;

    /**
     * 订单状态 (0-待支付, 1-已支付, 2-已完成, 3-已取消)
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 支付凭证URL
     */
    private String paymentProof;

    /**
     * 买家确认收货状态
     */
    private Boolean buyerConfirmed;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 完成时间
     */
    private Date finishTime;
}