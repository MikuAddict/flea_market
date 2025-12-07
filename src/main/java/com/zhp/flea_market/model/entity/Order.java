package com.zhp.flea_market.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单实体
 */
@Entity
@Table(name = "market_order")
@Data
public class Order {

    /**
     * 订单ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 商品ID
     */
    @Column(name = "product_id")
    private Long productId;

    /**
     * 买家ID
     */
    @Column(name = "buyer_id")
    private Long buyerId;

    /**
     * 卖家ID
     */
    @Column(name = "seller_id")
    private Long sellerId;

    /**
     * 关联商品
     */
    @TableField(exist = false)
    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    /**
     * 关联买家
     */
    @TableField(exist = false)
    @ManyToOne
    @JoinColumn(name = "buyer_id", insertable = false, updatable = false)
    private User buyer;

    /**
     * 关联卖家
     */
    @TableField(exist = false)
    @ManyToOne
    @JoinColumn(name = "seller_id", insertable = false, updatable = false)
    private User seller;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 支付方式 (0-现金, 1-微信, 2-积分兑换, 3-物品交换)
     */
    private Integer paymentMethod;

    /**
     * 订单状态 (0-待支付, 1-已支付, 2-已完成, 3-已取消)
     */
    private Integer status;

    /**
     * 支付凭证URL (现金支付时买家上传的支付凭证)
     */
    private String paymentProof;

    /**
     * 买家确认收货状态 (false-未确认, true-已确认)
     */
    private Boolean buyerConfirmed;

    /**
     * 卖家确认收款状态 (false-未确认, true-已确认)
     */
    private Boolean sellerConfirmed;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 完成时间
     */
    private Date finishTime;
}
