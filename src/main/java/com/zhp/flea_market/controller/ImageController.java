package com.zhp.flea_market.controller;

import com.zhp.flea_market.annotation.AuthCheck;
import com.zhp.flea_market.annotation.LoginRequired;
import com.zhp.flea_market.common.BaseResponse;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.common.ResultUtils;
import com.zhp.flea_market.constant.UserConstant;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.model.dto.response.ImageUploadResponse;
import com.zhp.flea_market.service.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片管理接口
 */
@RestController
@RequestMapping("/image")
@Tag(name = "图片管理", description = "图片上传、删除和管理接口")
@Slf4j
public class ImageController {

    @Autowired
    private ImageStorageService imageStorageService;

    /**
     * 上传用户头像
     */
    @Operation(summary = "上传用户头像", description = "用户上传个人头像图片")
    @PostMapping("/avatar")
    @LoginRequired
    public BaseResponse<ImageUploadResponse> uploadAvatar(
            @Parameter(description = "头像图片文件") @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        
        logOperation("上传用户头像", request);
        ImageUploadResponse response = imageStorageService.uploadImage(file, ImageStorageService.ImageType.AVATAR);
        return ResultUtils.success(response);
    }

    /**
     * 上传二手物品图片
     */
    @Operation(summary = "上传二手物品图片", description = "上传二手物品相关图片，支持单张或多张图片上传")
    @PostMapping("/product")
    @LoginRequired
    public BaseResponse<Object> uploadProductImage(
            @Parameter(description = "二手物品图片文件，支持单个文件或多个文件") @RequestParam("files") MultipartFile[] files,
            HttpServletRequest request) {
        
        if (files.length == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "至少需要上传一个文件");
        }
        
        if (files.length == 1) {
            // 单文件上传，返回单个结果
            logOperation("上传二手物品图片", request);
            ImageUploadResponse response = imageStorageService.uploadImage(files[0], ImageStorageService.ImageType.PRODUCT);
            return ResultUtils.success(response);
        } else {
            // 多文件上传，返回数组结果
            ImageUploadResponse[] responses = new ImageUploadResponse[files.length];
            for (int i = 0; i < files.length; i++) {
                responses[i] = imageStorageService.uploadImage(files[i], ImageStorageService.ImageType.PRODUCT);
            }
            
            logOperation("批量上传二手物品图片", request, "图片数量", files.length);
            return ResultUtils.success(responses);
        }
    }

    /**
     * 上传新闻配图
     */
    @Operation(summary = "上传新闻配图", description = "上传新闻文章配图")
    @PostMapping("/news")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<ImageUploadResponse> uploadNewsImage(
            @Parameter(description = "新闻配图文件") @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        
        logOperation("上传新闻配图", request);
        ImageUploadResponse response = imageStorageService.uploadImage(file, ImageStorageService.ImageType.NEWS);
        return ResultUtils.success(response);
    }

    /**
     * 通用图片上传
     */
    @Operation(summary = "通用图片上传", description = "通用图片上传接口，可指定图片类型")
    @PostMapping("")
    @LoginRequired
    public BaseResponse<ImageUploadResponse> uploadImage(
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "图片类型") @RequestParam("type") String type,
            HttpServletRequest request) {
        
        ImageStorageService.ImageType imageType;
        try {
            imageType = ImageStorageService.ImageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            imageType = ImageStorageService.ImageType.OTHER;
        }
        
        logOperation("上传通用图片", request, "图片类型", type);
        ImageUploadResponse response = imageStorageService.uploadImage(file, imageType);
        return ResultUtils.success(response);
    }

    /**
     * 批量上传图片
     */
    @Operation(summary = "批量上传图片", description = "批量上传多张图片")
    @PostMapping("/batch")
    @LoginRequired
    public BaseResponse<ImageUploadResponse[]> uploadBatchImages(
            @Parameter(description = "图片文件数组") @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "图片类型") @RequestParam("type") String type,
            HttpServletRequest request) {
        
        ImageStorageService.ImageType imageType;
        try {
            imageType = ImageStorageService.ImageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            imageType = ImageStorageService.ImageType.OTHER;
        }
        
        ImageUploadResponse[] responses = new ImageUploadResponse[files.length];
        for (int i = 0; i < files.length; i++) {
            responses[i] = imageStorageService.uploadImage(files[i], imageType);
        }
        
        logOperation("批量上传图片", request, "图片数量", files.length, "图片类型", type);
        return ResultUtils.success(responses);
    }

    /**
     * 删除图片
     */
    @Operation(summary = "删除图片", description = "根据图片URL删除图片")
    @DeleteMapping("")
    @LoginRequired
    public BaseResponse<Boolean> deleteImage(
            @Parameter(description = "图片URL") @RequestParam("imageUrl") String imageUrl,
            HttpServletRequest request) {
        
        boolean result = imageStorageService.deleteImage(imageUrl);
        
        logOperation("删除图片", result, request, "图片URL", imageUrl);
        return ResultUtils.success(result);
    }

    /**
     * 批量删除图片
     */
    @Operation(summary = "批量删除图片", description = "批量删除多张图片")
    @DeleteMapping("/batch")
    @LoginRequired
    public BaseResponse<Boolean> deleteBatchImages(
            @Parameter(description = "图片URL数组") @RequestParam("imageUrls") String[] imageUrls,
            HttpServletRequest request) {
        
        boolean allDeleted = true;
        for (String imageUrl : imageUrls) {
            boolean deleted = imageStorageService.deleteImage(imageUrl);
            if (!deleted) {
                allDeleted = false;
                log.warn("图片删除失败: {}", imageUrl);
            }
        }
        
        logOperation("批量删除图片", allDeleted, request, "图片数量", imageUrls.length);
        return ResultUtils.success(allDeleted);
    }

    /**
     * 记录操作日志
     */
    private void logOperation(String operation, HttpServletRequest request, Object... params) {
        StringBuilder logMessage = new StringBuilder(operation);
        
        if (params.length > 0) {
            logMessage.append(" - ");
            for (int i = 0; i < params.length; i += 2) {
                if (i > 0) logMessage.append(", ");
                logMessage.append(params[i]).append(": ").append(params[i + 1]);
            }
        }
        
        log.info(logMessage.toString());
    }

    /**
     * 记录操作日志（带结果）
     */
    private void logOperation(String operation, boolean result, HttpServletRequest request, Object... params) {
        StringBuilder logMessage = new StringBuilder(operation);
        logMessage.append(result ? "成功" : "失败");
        
        if (params.length > 0) {
            logMessage.append(" - ");
            for (int i = 0; i < params.length; i += 2) {
                if (i > 0) logMessage.append(", ");
                logMessage.append(params[i]).append(": ").append(params[i + 1]);
            }
        }
        
        log.info(logMessage.toString());
    }
}