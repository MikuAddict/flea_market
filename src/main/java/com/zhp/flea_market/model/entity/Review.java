package com.zhp.flea_market.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

/**
 * 商品评价实体
 */
@Entity
@Table(name = "review")
@Data
public class Review {

    /**
     * 评价ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联用户
     */
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User userId;

    /**
     * 关联商品
     */
    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product productId;

    /**
     * 关联订单
     */
    @ManyToOne
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order orderId;

    /**
     * 评分 (1-5分)
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;
}
