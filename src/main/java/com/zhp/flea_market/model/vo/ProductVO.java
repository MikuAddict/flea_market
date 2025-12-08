package com.zhp.flea_market.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 二手物品视图对象
 */
@Data
public class ProductVO {
    /**
     * 二手物品ID
     */
    private Long id;

    /**
     * 二手物品名称
     */
    private String productName;

    /**
     * 二手物品描述
     */
    private String description;

    /**
     * 二手物品价格
     */
    private BigDecimal price;

    /**
     * 二手物品图片地址
     */
    private String imageUrl;

    /**
     * 二手物品状态 (0-待审核, 1-已上架, 2-已下架, 3-已售出)
     */
    private Integer status;

    /**
     * 支付方式 (0-现金支付, 1-微信支付, 2-积分兑换, 3-二手物品交换)
     */
    private Integer paymentMethod;

    /**
     * 二手物品分类ID
     */
    private Long categoryId;

    /**
     * 二手物品分类名称
     */
    private String categoryName;

    /**
     * 发布者ID
     */
    private Long userId;

    /**
     * 发布者名称
     */
    private String userName;

    /**
     * 发布者头像
     */
    private String userAvatar;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}