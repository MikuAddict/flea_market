package com.zhp.flea_market.service.impl;

import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.config.ImageStorageConfig;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.model.dto.response.ImageUploadResponse;
import com.zhp.flea_market.service.ImageStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 图片存储服务实现类
 */
@Service
@Slf4j
public class ImageStorageServiceImpl implements ImageStorageService {

    @Autowired
    private ImageStorageConfig storageConfig;

    /**
     * 上传图片
     */
    @Override
    public ImageUploadResponse uploadImage(MultipartFile file, ImageType imageType) {
        try {
            // 验证文件
            validateFile(file);
            
            // 生成存储路径
            String storagePath = generateStoragePath(file.getOriginalFilename(), imageType);
            log.info("准备上传文件到路径: {}", storagePath);
            
            // 创建目录
            createDirectories(storagePath);
            
            // 保存原始图片
            File targetFile = new File(storagePath);
            file.transferTo(targetFile);
            // 生成访问URL
            String originalUrl = generateImageUrl(storagePath);
            Map<String, String> thumbnailUrls = new HashMap<>();
            
            log.info("图片上传成功: {}, 类型: {}", originalUrl, imageType);
            
            return ImageUploadResponse.builder()
                    .originalUrl(originalUrl)
                    .thumbnailUrls(thumbnailUrls) // 空的缩略图URL集合
                    .fileSize(file.getSize())
                    .format(getFileExtension(file.getOriginalFilename()))
                    .uploadTime(new Date())
                    .build();
                    
        } catch (IOException e) {
            log.error("图片上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除图片
     */
    @Override
    public boolean deleteImage(String imageUrl) {
        try {
            String filePath = extractFilePathFromUrl(imageUrl);
            File file = new File(filePath);
            
            if (file.exists()) {
                boolean deleted = file.delete();
                log.info("图片删除成功: {}", filePath);
                return deleted;
            }
            
            return false;
        } catch (Exception e) {
            log.error("图片删除失败: {}", imageUrl, e);
            return false;
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        
        if (file.getSize() > storageConfig.getMaxFileSize() * 1024 * 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, 
                String.format("文件大小不能超过 %dMB", storageConfig.getMaxFileSize()));
        }
        
        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (!isAllowedFormat(fileExtension)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, 
                String.format("不支持的文件格式: %s", fileExtension));
        }
    }

    /**
     * 生成存储路径
     */
    private String generateStoragePath(String originalFilename, ImageType imageType) {
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID() + "." + extension;

        String fullPath = String.format("%s/%s/%s", 
            storageConfig.getBasePath(), 
            imageType.getFolderName(), 
            filename);
            
        // 使用系统相关的路径分隔符，并确保使用绝对路径
        return new File(fullPath).getAbsolutePath();
    }

    /**
     * 创建目录
     */
    private void createDirectories(String filePath) throws IOException {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("无法创建目录: " + parentDir.getAbsolutePath());
            }
        }
    }

    /**
     * 生成图片URL
     */
    private String generateImageUrl(String filePath) {
        // 将绝对路径转换为相对于basePath的路径
        File baseDir = new File(storageConfig.getBasePath());
        File imageFile = new File(filePath);
        String relativePath = baseDir.toURI().relativize(imageFile.toURI()).getPath();
        return storageConfig.getBaseUrl() + "/" + relativePath.replace("\\", "/");
    }

    /**
     * 从URL提取文件路径
     */
    private String extractFilePathFromUrl(String imageUrl) {
        // 从URL中移除baseUrl部分，得到相对路径
        String relativePath = imageUrl.replaceFirst("^" + storageConfig.getBaseUrl() + "/?", "");
        // 构建完整文件路径
        return new File(storageConfig.getBasePath(), relativePath).getAbsolutePath();
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 检查是否支持的格式
     */
    private boolean isAllowedFormat(String extension) {
        return Arrays.asList(storageConfig.getAllowedFormats()).contains(extension.toLowerCase());
    }

}