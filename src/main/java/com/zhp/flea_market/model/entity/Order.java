package com.zhp.flea_market.model.entity;

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
    private Long productId;

    /**
     * 买家ID
     */
    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private User buyer;

    /**
     * 卖家ID
     */
    @ManyToOne
    @JoinColumn(name = "seller_id")
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
     * 创建时间
     */
    private Date createTime;

    /**
     * 完成时间
     */
    private Date finishTime;
}
