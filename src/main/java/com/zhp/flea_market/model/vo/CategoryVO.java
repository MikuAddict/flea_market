package com.zhp.flea_market.model.vo;

import lombok.Data;

/**
 * 商品分类视图对象
 */
@Data
public class CategoryVO {
    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 该分类下的商品总数
     */
    private Long productCount;
}