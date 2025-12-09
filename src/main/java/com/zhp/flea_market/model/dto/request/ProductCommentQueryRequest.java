package com.zhp.flea_market.model.dto.request;

import lombok.Data;

/**
 * 二手物品留言查询请求
 */
@Data
public class ProductCommentQueryRequest {

    /**
     * 当前页码
     */
    private int current = 1;

    /**
     * 每页大小
     */
    private int pageSize = 10;
    
    /**
     * 二手物品ID
     */
    private Long productId;
    
    /**
     * 留言用户ID
     */
    private Long userId;
    
    /**
     * 父留言ID
     */
    private Long parentId;
    
    /**
     * 排序字段
     */
    private String sortField;
    
    /**
     * 排序顺序
     */
    private String sortOrder = "desc";
}