package com.zhp.flea_market.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

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
     * 订单信息
     */
    @ManyToOne
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order orderId;

    /**
     * 商品信息
     */
    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product productId;

    /**
     * 买家信息
     */
    @ManyToOne
    @JoinColumn(name = "buyer_id", insertable = false, updatable = false)
    private User buyerId;



    /**
     * 卖家信息
     */
    @ManyToOne
    @JoinColumn(name = "seller_id", insertable = false, updatable = false)
    private User sellerId;


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
     * 评价信息
     */
    @ManyToOne
    @JoinColumn(name = "review_id", insertable = false, updatable = false)
    private Review reviewId;

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