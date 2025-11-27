package com.zhp.flea_market.aspect;

import com.zhp.flea_market.annotation.AuthCheck;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.constant.UserConstant;
import com.zhp.flea_market.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 权限校验 AOP
 */
@Aspect
@Component
public class AuthAspect {

    /**
     * 执行权限校验
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(authCheck)")
    public Object doAuthCheck(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 获取当前请求
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        HttpServletRequest request = requestAttributes.getRequest();
        
        // 获取用户角色（通过JWT验证后存入request中）
        String userRole = (String) request.getAttribute("userRole");
        
        // 如果没有获取到用户角色，说明未登录
        if (userRole == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        
        // 获取注解中要求的角色
        String mustRole = authCheck.mustRole();
        
        // 如果没有指定角色要求，直接通过
        if (mustRole.isEmpty()) {
            return joinPoint.proceed();
        }
        
        // 检查用户角色是否符合要求
        if (!mustRole.equals(userRole) && !UserConstant.ADMIN_ROLE.equals(userRole)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "权限不足，需要" + mustRole + "权限");
        }
        
        // 权限通过，执行方法
        return joinPoint.proceed();
    }
}