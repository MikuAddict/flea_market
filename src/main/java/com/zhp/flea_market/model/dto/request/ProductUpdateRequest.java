package com.zhp.flea_market.model.dto.request;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 二手物品更新请求
 */
@Data
public class ProductUpdateRequest {
    
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
     * 二手物品分类ID
     */
    private Long categoryId;
    
    /**
     * 二手物品状态
     */
    private Integer status;
    
    /**
     * 支付方式 (0-现金支付, 1-微信支付, 2-积分兑换, 3-二手物品交换)
     */
    private Integer paymentMethod;
}