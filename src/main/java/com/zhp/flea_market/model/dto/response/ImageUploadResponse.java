package com.zhp.flea_market.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

/**
 * 图片上传响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponse {
    
    /**
     * 原始图片URL
     */
    private String originalUrl;
    
    /**
     * 缩略图URL集合（已移除缩略图功能，该字段始终为空）
     */
    private Map<String, String> thumbnailUrls;
    
    /**
     * 文件大小（字节）
     */
    private long fileSize;
    
    /**
     * 图片格式
     */
    private String format;
    
    /**
     * 上传时间
     */
    private Date uploadTime;
}