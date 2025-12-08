package com.zhp.flea_market.service;

import com.zhp.flea_market.model.dto.response.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片存储服务接口
 */
public interface ImageStorageService {

    /**
     * 上传图片
     * @param file 图片文件
     * @param imageType 图片类型
     * @return 上传响应信息
     */
    ImageUploadResponse uploadImage(MultipartFile file, ImageType imageType);

    /**
     * 删除图片
     * @param imageUrl 图片URL
     * @return 是否删除成功
     */
    boolean deleteImage(String imageUrl);

    /**
     * 图片类型枚举
     */
    enum ImageType {
        AVATAR("avatar", "用户头像"),
        PRODUCT("product", "二手物品图片"),
        NEWS("news", "新闻配图"),
        BANNER("banner", "横幅图片"),
        OTHER("other", "其他图片");

        private final String folderName;
        private final String description;

        ImageType(String folderName, String description) {
            this.folderName = folderName;
            this.description = description;
        }

        public String getFolderName() {
            return folderName;
        }

        public String getDescription() {
            return description;
        }
    }
}