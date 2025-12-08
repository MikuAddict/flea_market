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
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 添加时间
     */
    private Date createTime;
}