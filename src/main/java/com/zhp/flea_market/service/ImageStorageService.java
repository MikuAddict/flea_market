package com.zhp.flea_market.service;

import com.zhp.flea_market.config.ImageStorageConfig;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.model.dto.response.ImageUploadResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 图片存储服务
 */
@Service
@Slf4j
public class ImageStorageService {

    @Autowired
    private ImageStorageConfig storageConfig;

    /**
     * 上传图片
     */
    public ImageUploadResponse uploadImage(MultipartFile file, ImageType imageType) {
        try {
            // 验证文件
            validateFile(file);
            
            // 生成存储路径
            String storagePath = generateStoragePath(file.getOriginalFilename(), imageType);
            
            // 创建目录
            createDirectories(storagePath);
            
            // 保存原始图片
            File targetFile = new File(storagePath);
            file.transferTo(targetFile);
            
            // 生成缩略图
            Map<String, String> thumbnails = generateThumbnails(targetFile, storagePath);
            
            // 生成访问URL
            String originalUrl = generateImageUrl(storagePath);
            Map<String, String> thumbnailUrls = generateThumbnailUrls(thumbnails);
            
            log.info("图片上传成功: {}, 类型: {}", originalUrl, imageType);
            
            return ImageUploadResponse.builder()
                    .originalUrl(originalUrl)
                    .thumbnailUrls(thumbnailUrls)
                    .fileSize(file.getSize())
                    .format(getFileExtension(file.getOriginalFilename()))
                    .uploadTime(new Date())
                    .build();
                    
        } catch (IOException e) {
            log.error("图片上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片上传失败");
        }
    }

    /**
     * 删除图片
     */
    public boolean deleteImage(String imageUrl) {
        try {
            String filePath = extractFilePathFromUrl(imageUrl);
            File file = new File(filePath);
            
            if (file.exists()) {
                // 删除原始图片
                boolean deleted = file.delete();
                
                // 删除缩略图
                deleteThumbnails(filePath);
                
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
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String datePath = dateFormat.format(new Date());
        
        return String.format("%s/%s/%s/%s", 
            storageConfig.getBasePath(), 
            imageType.getFolderName(), 
            datePath, 
            filename);
    }

    /**
     * 生成缩略图
     */
    private Map<String, String> generateThumbnails(File originalFile, String originalPath) {
        if (!storageConfig.isGenerateThumbnails()) {
            return new HashMap<>();
        }
        
        Map<String, String> thumbnails = new HashMap<>();
        
        try {
            BufferedImage originalImage = ImageIO.read(originalFile);
            
            for (ImageStorageConfig.ThumbnailSize size : storageConfig.getThumbnailSizes()) {
                String thumbnailPath = generateThumbnailPath(originalPath, size.getName());
                
                BufferedImage thumbnail = resizeImage(originalImage, size.getWidth(), size.getHeight());
                ImageIO.write(thumbnail, getFileExtension(originalPath), new File(thumbnailPath));
                
                thumbnails.put(size.getName(), thumbnailPath);
            }
            
        } catch (IOException e) {
            log.warn("缩略图生成失败: {}", originalPath, e);
        }
        
        return thumbnails;
    }

    /**
     * 调整图片尺寸
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // 计算缩放比例
        double scale = Math.min((double) targetWidth / originalWidth, (double) targetHeight / originalHeight);
        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);
        
        BufferedImage resizedImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resizedImage.createGraphics();
        
        // 设置渲染质量
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        graphics.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        graphics.dispose();
        
        return resizedImage;
    }

    /**
     * 创建目录
     */
    private void createDirectories(String filePath) throws IOException {
        Path path = Paths.get(filePath).getParent();
        if (path != null) {
            Files.createDirectories(path);
        }
    }

    /**
     * 生成图片URL
     */
    private String generateImageUrl(String filePath) {
        String relativePath = filePath.replace(storageConfig.getBasePath() + "/", "");
        return storageConfig.getBaseUrl() + "/" + relativePath;
    }

    /**
     * 生成缩略图URL
     */
    private Map<String, String> generateThumbnailUrls(Map<String, String> thumbnailPaths) {
        Map<String, String> thumbnailUrls = new HashMap<>();
        
        for (Map.Entry<String, String> entry : thumbnailPaths.entrySet()) {
            String url = generateImageUrl(entry.getValue());
            thumbnailUrls.put(entry.getKey(), url);
        }
        
        return thumbnailUrls;
    }

    /**
     * 删除缩略图
     */
    private void deleteThumbnails(String originalPath) {
        for (ImageStorageConfig.ThumbnailSize size : storageConfig.getThumbnailSizes()) {
            String thumbnailPath = generateThumbnailPath(originalPath, size.getName());
            File thumbnailFile = new File(thumbnailPath);
            if (thumbnailFile.exists()) {
                thumbnailFile.delete();
            }
        }
    }

    /**
     * 生成缩略图路径
     */
    private String generateThumbnailPath(String originalPath, String sizeName) {
        String extension = getFileExtension(originalPath);
        String basePath = originalPath.substring(0, originalPath.lastIndexOf('.'));
        return basePath + "_" + sizeName + "." + extension;
    }

    /**
     * 从URL提取文件路径
     */
    private String extractFilePathFromUrl(String imageUrl) {
        String relativePath = imageUrl.replace(storageConfig.getBaseUrl() + "/", "");
        return storageConfig.getBasePath() + "/" + relativePath;
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

    /**
     * 图片类型枚举
     */
    @Getter
    public enum ImageType {
        AVATAR("avatar", "用户头像"),
        PRODUCT("product", "商品图片"),
        NEWS("news", "新闻配图"),
        BANNER("banner", "横幅图片"),
        OTHER("other", "其他图片");

        private final String folderName;
        private final String description;

        ImageType(String folderName, String description) {
            this.folderName = folderName;
            this.description = description;
        }

    }
}