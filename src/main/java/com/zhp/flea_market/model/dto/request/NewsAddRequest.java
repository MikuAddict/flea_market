package com.zhp.flea_market.model.dto.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 新闻添加请求
 */
@Data
public class NewsAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

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
}