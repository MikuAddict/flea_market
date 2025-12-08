package com.zhp.flea_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 平台公告实体
 */
@TableName("news")
@Data
public class News {
    /**
     * 平台公告ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 公告图片地址
     */
    private String imageUrl;

    /**
     * 公告作者ID
     */
    private Long authorId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    /**
     * 逻辑删除字段
     */
    @TableLogic
    private Integer deleted = 0;
}
