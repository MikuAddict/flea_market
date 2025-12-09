package com.zhp.flea_market.model.dto.request;

import lombok.Data;

/**
 * 添加二手物品留言请求
 */
@Data
public class ProductCommentAddRequest {

    /**
     * 二手物品ID
     */
    private Long productId;

    /**
     * 留言内容
     */
    private String content;

    /**
     * 父留言ID（0表示一级留言，非0表示回复的留言）
     */
    private Long parentId = 0L;

    /**
     * 回复的用户ID（被回复的用户ID，0表示不是回复具体用户）
     */
    private Long replyUserId = 0L;
}