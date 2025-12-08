package com.zhp.flea_market.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 评价视图对象
 */
@Data
public class ReviewVO {
    /**
     * 评价ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 订单ID
     */
    private Long orderId;

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