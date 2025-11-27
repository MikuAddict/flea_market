package com.zhp.flea_market.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
     * 新闻作者ID
     */
    @Column(name = "author_id")
    private Long authorId;

    /**
     * 新闻作者
     */
    @ManyToOne
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private User author;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
