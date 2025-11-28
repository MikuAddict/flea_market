package com.zhp.flea_market.model.dto.request.product;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 商品更新请求
 */
@Data
public class ProductUpdateRequest {
    
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
     * 商品图片地址
     */
    private String imageUrl;
    
    /**
     * 商品分类ID
     */
    private Long categoryId;
    
    /**
     * 商品状态
     */
    private Integer status;
}