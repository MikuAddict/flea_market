package com.zhp.flea_market.model.dto.request.product;

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
}