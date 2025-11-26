package com.zhp.flea_market.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "news")
@Data
public class News {
    /**
     * 新闻ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 新闻标题
     */
    private String title;

    /**
     * 新闻内容
     */
    private String content;

    /**
     * 新闻图片地址
     */
    private String imageUrl;

    /**
     * 新闻作者
     */
    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;
    /**
     * 创建时间
     */
    private Date createTime;
}
