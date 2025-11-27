package com.zhp.flea_market.config;

import com.zhp.flea_market.annotation.AuthCheck;
import com.zhp.flea_market.annotation.LoginRequired;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.constant.UserConstant;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 自动化认证拦截器
 * 统一处理登录状态和权限验证
 */
@Slf4j
@Component
public class AutomatedAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是Controller方法，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 检查是否有LoginRequired注解
        LoginRequired loginRequired = handlerMethod.getMethodAnnotation(LoginRequired.class);
        
        // 检查是否有AuthCheck注解
        AuthCheck authCheck = handlerMethod.getMethodAnnotation(AuthCheck.class);
        
        // 如果两个注解都没有，直接放行
        if (loginRequired == null && authCheck == null) {
            return true;
        }

        // 获取当前登录用户
        User currentUser = null;
        try {
            // 使用permitNull方法获取用户，避免抛出异常
            currentUser = userService.getLoginUserPermitNull(request);
        } catch (Exception e) {
            log.warn("获取登录用户信息失败: {}", e.getMessage());
        }

        // 处理LoginRequired注解
        if (loginRequired != null) {
            if (!loginRequired.permitNull() && currentUser == null) {
                log.error("接口需要登录，但用户未登录");
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
            }
        }

        // 处理AuthCheck注解
        if (authCheck != null) {
            // 先检查是否登录
            if (currentUser == null) {
                log.error("权限验证失败：用户未登录");
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
            }

            String mustRole = authCheck.mustRole();
            if (!mustRole.isEmpty()) {
                // 检查用户角色是否符合要求
                boolean hasPermission = mustRole.equals(currentUser.getUserRole()) || 
                                      UserConstant.ADMIN_ROLE.equals(currentUser.getUserRole());
                
                if (!hasPermission) {
                    log.error("权限验证失败：用户[{}]没有[{}]权限", currentUser.getUserName(), mustRole);
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, 
                        String.format("权限不足，需要%s权限", mustRole));
                }
            }
        }

        // 将用户信息存入请求属性，供后续使用
        if (currentUser != null) {
            request.setAttribute("currentUser", currentUser);
            request.setAttribute("userRole", currentUser.getUserRole());
        }

        log.debug("权限验证通过，用户: {}", currentUser != null ? currentUser.getUserName() : "未登录");
        return true;
    }
}