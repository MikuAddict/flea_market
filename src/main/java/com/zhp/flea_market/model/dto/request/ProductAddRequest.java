package com.zhp.flea_market.model.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 二手物品添加请求
 */
@Data
public class ProductAddRequest {
    
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
     * 二手物品图片地址列表
     */
    private List<String> imageUrls;
    
    /**
     * 二手物品分类ID
     */
    private Long categoryId;
    
    /**
     * 支付方式 (0-现金支付, 1-微信支付, 2-积分兑换, 3-二手物品交换)
     */
    private Integer paymentMethod;
}