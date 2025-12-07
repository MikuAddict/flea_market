package com.zhp.flea_market.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

/**
 * 平台公告实体
 */
@Entity
@Table(name = "news")
@Data
public class News {
    /**
     * 平台公告ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @TableField(exist = false)
    @ManyToOne
    @JoinColumn(name = "author_id")
    private User user;


    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
