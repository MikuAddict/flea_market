package com.zhp.flea_market.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhp.flea_market.common.BaseResponse;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.common.ResultUtils;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.exception.ThrowUtils;
import com.zhp.flea_market.model.dto.request.PageRequest;
import com.zhp.flea_market.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 基础控制器抽象类
 * 提供通用的参数校验、异常处理和日志记录功能
 */
@Slf4j
public abstract class BaseController {

    @Resource
    protected UserService userService;

    /**
     * 分页查询通用参数校验
     *
     * @param current 当前页码
     * @param size 每页大小
     * @return Page对象
     */
    protected <T> Page<T> validatePageParams(long current, long size) {
        // 页码限制
        if (current < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "页码不能小于1");
        }
        // 页面大小限制
        if (size < 1 || size > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "每页大小必须在1-100之间");
        }
        return new Page<>(current, size);
    }

    /**
     * 分页查询通用参数校验（带限制）
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param maxSize 最大页面大小
     * @return Page对象
     */
    protected <T> Page<T> validatePageParams(long current, long size, int maxSize) {
        // 页码限制
        if (current < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "页码不能小于1");
        }
        // 页面大小限制
        if (size < 1 || size > maxSize) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "每页大小必须在1-" + maxSize + "之间");
        }
        return new Page<>(current, size);
    }

    /**
     * ID参数校验
     *
     * @param id ID值
     * @param idName ID名称，用于错误消息
     */
    protected void validateId(Long id, String idName) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, idName + "无效");
        }
    }

    /**
     * 非空字符串校验
     *
     * @param value 待校验的值
     * @param fieldName 字段名称，用于错误消息
     */
    protected void validateNotBlank(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, fieldName + "不能为空");
        }
    }

    /**
     * 对象非空校验
     *
     * @param obj 待校验的对象
     * @param objName 对象名称，用于错误消息
     */
    protected void validateNotNull(Object obj, String objName) {
        if (obj == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, objName + "不能为空");
        }
    }

    /**
     * 创建分页查询请求对象
     *
     * @param current 基础分页请求
     * @return 具体的查询请求对象
     */
    protected <T extends PageRequest> T createPageQueryRequest(Class<T> clazz, long current, long size) {
        try {
            T request = clazz.getDeclaredConstructor().newInstance();
            request.setCurrent((int) current);
            request.setPageSize((int) size);
            return request;
        } catch (Exception e) {
            log.error("创建分页查询请求对象失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 操作结果校验并返回响应
     *
     * @param result 操作结果
     * @param successMessage 成功消息
     * @param <T> 返回数据类型
     * @return 响应对象
     */
    protected <T> BaseResponse<T> handleOperationResult(boolean result, String successMessage, T data) {
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 创建带有成功消息的响应
        BaseResponse<T> response = ResultUtils.success(data);
        response.setMessage(successMessage);
        return response;
    }

    /**
     * 操作结果校验并返回响应
     *
     * @param result 操作结果
     * @param <T> 返回数据类型
     * @return 响应对象
     */
    protected <T> BaseResponse<T> handleOperationResult(boolean result, T data) {
        return handleOperationResult(result, "操作成功", data);
    }

    /**
     * 操作结果校验并返回响应（无数据）
     *
     * @param result 操作结果
     * @param successMessage 成功消息
     * @return 响应对象
     */
    protected BaseResponse<Boolean> handleOperationResult(boolean result, String successMessage) {
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 创建带有成功消息的响应
        BaseResponse<Boolean> response = ResultUtils.success(true);
        response.setMessage(successMessage);
        return response;
    }

    /**
     * 记录操作日志
     *
     * @param operation 操作名称
     * @param request HTTP请求
     * @param params 参数
     */
    protected void logOperation(String operation, HttpServletRequest request, Object... params) {
        String username = "anonymous";
        try {
            username = userService.getLoginUser(request).getUserName();
        } catch (Exception e) {
            // 忽略获取用户名失败的情况
        }
        log.info("用户[{}]执行操作: {} - 参数: {}", username, operation, params);
    }

    /**
     * 记录操作日志（带结果）
     *
     * @param operation 操作名称
     * @param success 是否成功
     * @param request HTTP请求
     * @param params 参数
     */
    protected void logOperation(String operation, boolean success, HttpServletRequest request, Object... params) {
        String username = "anonymous";
        try {
            username = userService.getLoginUser(request).getUserName();
        } catch (Exception e) {
            // 忽略获取用户名失败的情况
        }
        log.info("用户[{}]执行操作: {} - 结果: {} - 参数: {}", username, operation, success ? "成功" : "失败", params);
    }

    /**
     * 检查资源是否存在
     *
     * @param resource 资源对象
     * @param resourceName 资源名称
     */
    protected void validateResourceExists(Object resource, String resourceName) {
        if (resource == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, resourceName + "不存在");
        }
    }
}