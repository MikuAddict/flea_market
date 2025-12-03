package com.zhp.flea_market.exception;

import com.zhp.flea_market.common.BaseResponse;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.common.ResultUtils;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.service.OperationLogService;
import com.zhp.flea_market.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 业务异常处理
     *
     * @param e 业务异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常: {}", e.getMessage(), e);
        
        // 记录操作日志
        logErrorToOperationLog(e, request);
        
        return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());
    }

    /**
     * 认证异常处理
     *
     * @param e 认证异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(AuthenticationException.class)
    public BaseResponse<?> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.error("认证异常: {}", e.getMessage(), e);
        
        // 记录操作日志
        logErrorToOperationLog(e, request);
        
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "认证失败，请重新登录");
    }

    /**
     * 授权异常处理
     *
     * @param e 授权异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public BaseResponse<?> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.error("授权异常: {}", e.getMessage(), e);
        
        // 记录操作日志
        logErrorToOperationLog(e, request);
        
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "权限不足，拒绝访问");
    }

    /**
     * 参数校验异常处理（@RequestBody）
     *
     * @param e 参数校验异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("参数校验异常: {}", e.getMessage(), e);
        
        // 获取所有校验错误信息
        List<String> errors = e.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        String errorMessage = String.join(", ", errors);
        
        // 记录操作日志
        logErrorToOperationLog(e, request);
        
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, "参数校验失败: " + errorMessage);
    }

    /**
     * 参数校验异常处理（表单）
     *
     * @param e 参数校验异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    public BaseResponse<?> handleBindException(BindException e, HttpServletRequest request) {
        log.error("参数绑定异常: {}", e.getMessage(), e);
        
        // 获取所有校验错误信息
        List<String> errors = e.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        String errorMessage = String.join(", ", errors);
        
        // 记录操作日志
        logErrorToOperationLog(e, request);
        
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, "参数校验失败: " + errorMessage);
    }

    /**
     * 主键冲突异常处理
     *
     * @param e 主键冲突异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public BaseResponse<?> handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        log.error("主键冲突异常: {}", e.getMessage(), e);
        
        // 记录操作日志
        logErrorToOperationLog(e, request);
        
        return ResultUtils.error(ErrorCode.OPERATION_ERROR, "数据已存在，请勿重复提交");
    }

    /**
     * 运行时异常处理
     *
     * @param e 运行时异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常: {}", e.getMessage(), e);
        
        // 记录操作日志
        logErrorToOperationLog(e, request);
        
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统异常，请稍后重试");
    }

    /**
     * 通用异常处理
     *
     * @param e 异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {}", e.getMessage(), e);
        
        // 记录操作日志
        logErrorToOperationLog(e, request);
        
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统异常，请稍后重试");
    }

    /**
     * 记录异常到操作日志
     *
     * @param e 异常
     * @param request HTTP请求
     */
    private void logErrorToOperationLog(Exception e, HttpServletRequest request) {
        try {
            // 获取当前用户
            User currentUser = userService.getLoginUserPermitNull(request);
            
            Long userId = null;
            String userName = null;
            
            if (currentUser != null) {
                userId = currentUser.getId();
                userName = currentUser.getUserName();
            }
            
            // 获取异常信息
            String errorMessage = e.getMessage();
            String operationContent = "请求URL: " + request.getRequestURI();
            
            // 记录操作日志
            operationLogService.logOperation(
                    userId, userName, request,
                    4, // 操作类型：删除
                    "异常处理",
                    "系统",
                    operationContent,
                    0, // 操作结果：失败
                    "操作异常",
                    errorMessage
            );
        } catch (Exception logException) {
            // 避免记录日志时出现递归异常
            log.error("记录操作日志失败: {}", logException.getMessage(), logException);
        }
    }
}