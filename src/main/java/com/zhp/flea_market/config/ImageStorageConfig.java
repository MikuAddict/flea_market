package com.zhp.flea_market.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 图片存储配置
 */
@Configuration
@ConfigurationProperties(prefix = "image.storage")
@Data
public class ImageStorageConfig {
    
    /**
     * 存储根路径
     */
    private String basePath = "uploads/images";
    
    /**
     * 图片访问基础URL
     */
    private String baseUrl = "/api/images";
    
    /**
     * 支持的最大文件大小（MB）
     */
    private int maxFileSize = 10;
    
    /**
     * 支持的图片格式
     */
    private String[] allowedFormats = {"jpg", "jpeg", "png", "webp", "gif"};
    
    /**
     * 是否启用图片压缩
     */
    private boolean enableCompression = true;
    
    /**
     * 压缩质量（0-1）
     */
    private double compressionQuality = 0.8;
    
    // 删除缩略图相关配置
    
    /*
    @Data
    public static class ThumbnailSize {
        private String name;
        private int width;
        private int height;
        
        public ThumbnailSize(String name, int width, int height) {
            this.name = name;
            this.width = width;
            this.height = height;
        }
    }
    */
}