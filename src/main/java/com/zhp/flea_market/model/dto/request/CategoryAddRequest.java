package com.zhp.flea_market.model.dto.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品分类添加请求
 */
@Data
public class CategoryAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类名称
     */
    private String name;
}