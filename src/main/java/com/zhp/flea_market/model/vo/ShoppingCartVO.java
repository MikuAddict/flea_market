package com.zhp.flea_market.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 购物车视图对象
 */
@Data
public class ShoppingCartVO {
    /**
     * 购物车项ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 二手物品ID
     */
    private Long productId;

    /**
     * 二手物品名称
     */
    private String productName;

    /**
     * 主图URL（第一张图片）
     */
    private String mainImageUrl;

    /**
     * 二手物品价格
     */
    private BigDecimal price;

    /**
     * 二手物品描述
     */
    private String description;

    /**
     * 添加时间
     */
    private Date createTime;
}