package com.zhp.flea_market.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 交易记录实体
 */
@Entity
@Table(name = "trade_record")
@Data
public class TradeRecord {

    /**
     * 交易记录ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 订单ID
     */
    @Column(name = "order_id")
    private Long orderId;

    /**
     * 商品ID
     */
    @Column(name = "product_id")
    private Long productId;

    /**
     * 商品名称
     */
    @Column(name = "product_name")
    private String productName;

    /**
     * 买家ID
     */
    @Column(name = "buyer_id")
    private Long buyerId;

    /**
     * 买家用户名
     */
    @Column(name = "buyer_name")
    private String buyerName;

    /**
     * 卖家ID
     */
    @Column(name = "seller_id")
    private Long sellerId;

    /**
     * 卖家用户名
     */
    @Column(name = "seller_name")
    private String sellerName;

    /**
     * 交易金额
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * 支付方式 (0-现金, 1-微信, 2-积分兑换, 3-物品交换)
     */
    @Column(name = "payment_method")
    private Integer paymentMethod;

    /**
     * 支付方式描述
     */
    @Column(name = "payment_method_desc")
    private String paymentMethodDesc;

    /**
     * 交易时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "trade_time")
    private Date tradeTime;

    /**
     * 交易状态 (1-成功, 2-已完成评价, 3-已退款)
     */
    @Column(name = "trade_status")
    private Integer tradeStatus;

    /**
     * 交易备注
     */
    private String remark;

    /**
     * 评价ID（如果已评价）
     */
    @Column(name = "review_id")
    private Long reviewId;

    /**
     * 在保存实体之前设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        if (tradeTime == null) {
            tradeTime = new Date();
        }
    }
}