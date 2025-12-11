package com.zhp.flea_market.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 商品视图对象
 */
@Data
public class ProductVO {
    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 主图URL（第一张图片）
     */
    private String mainImageUrl;

    /**
     * 所有图片URL列表
     */
    private List<String> imageUrls;

    /**
     * 商品状态 (0-待审核, 1-已上架, 2-已下架, 3-已售出)
     */
    private Integer status;

    /**
     * 支付方式 (0-现金支付, 1-微信支付, 2-积分兑换, 3-二手物品交换)
     */
    private Integer paymentMethod;

    /**
     * 商品分类ID
     */
    private Long categoryId;

    /**
     * 商品分类名称
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
     * 发布者手机号
     */
    private String userPhone;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}