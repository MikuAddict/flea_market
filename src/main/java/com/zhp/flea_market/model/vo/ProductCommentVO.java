package com.zhp.flea_market.model.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 二手物品留言视图对象
 */
@Data
public class ProductCommentVO {

    /**
     * 留言ID
     */
    private Long id;

    /**
     * 二手物品ID
     */
    private Long productId;

    /**
     * 留言用户ID
     */
    private Long userId;

    /**
     * 留言用户名
     */
    private String userName;

    /**
     * 留言用户头像
     */
    private String userAvatar;

    /**
     * 留言内容
     */
    private String content;

    /**
     * 父留言ID
     */
    private Long parentId;

    /**
     * 回复的用户ID
     */
    private Long replyUserId;

    /**
     * 回复的用户名
     */
    private String replyUserName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 子留言列表
     */
    private List<ProductCommentVO> children;
}