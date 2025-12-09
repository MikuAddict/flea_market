package com.zhp.flea_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

/**
 * 二手物品留言实体
 */
@TableName("product_comment")
@Data
public class ProductComment {

    /**
     * 留言ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    
    /**
     * 逻辑删除字段
     */
    @TableLogic
    private Integer deleted = 0;
}