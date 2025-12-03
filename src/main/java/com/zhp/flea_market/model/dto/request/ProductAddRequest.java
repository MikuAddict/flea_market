package com.zhp.flea_market.model.dto.request;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 商品添加请求
 */
@Data
public class ProductAddRequest {
    
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
     * 商品图片地址
     */
    private String imageUrl;
    
    /**
     * 商品分类ID
     */
    private Long categoryId;
    
    /**
     * 支付方式 (0-现金支付, 1-微信支付, 2-积分兑换, 3-二手物品交换)
     */
    private Integer paymentMethod;
}