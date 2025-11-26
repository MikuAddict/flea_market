package com.zhp.flea_market.entity;

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
     * 商品ID
     */
    private Long productId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 评价用户ID
     */
    private Long userId;

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
