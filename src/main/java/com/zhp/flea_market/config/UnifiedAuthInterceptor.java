package com.zhp.flea_market.config;

import com.zhp.flea_market.annotation.AuthCheck;
import com.zhp.flea_market.annotation.LoginRequired;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.common.JwtKit;
import com.zhp.flea_market.common.JwtProperties;
import com.zhp.flea_market.constant.UserConstant;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 统一认证拦截器
 * 整合JWT认证、权限验证和SecurityContext设置
 */
@Slf4j
@Component
public class UnifiedAuthInterceptor implements HandlerInterceptor {

    private final JwtKit jwtKit;
    private final JwtProperties jwtProperties;
    private final UserService userService;

    public UnifiedAuthInterceptor(JwtKit jwtKit, JwtProperties jwtProperties, UserService userService) {
        this.jwtKit = jwtKit;
        this.jwtProperties = jwtProperties;
        this.userService = userService;
    }

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
        
        // 执行JWT认证
        User currentUser = performJwtAuthentication(request);
        
        // 处理权限验证
        handleAuthorization(loginRequired, authCheck, currentUser, request);
        
        // 设置用户信息到请求属性
        if (currentUser != null) {
            request.setAttribute("currentUser", currentUser);
            request.setAttribute("userRole", currentUser.getUserRole());
        }

        log.debug("统一认证拦截器处理完成，用户: {}", currentUser != null ? currentUser.getUserName() : "未登录");
        return true;
    }

    /**
     * 执行JWT认证
     */
    private User performJwtAuthentication(HttpServletRequest request) {
        // 放行OPTIONS请求
        if ("OPTIONS".equals(request.getMethod())) {
            log.debug("放行OPTIONS请求");
            return null;
        }

        // 1. 优先从Cookie中获取token
        String jwtToken = getTokenFromCookie(request);
        
        // 2. 如果Cookie中没有，再从请求头中获取
        if (jwtToken == null) {
            final String requestTokenHeader = request.getHeader(jwtProperties.getTokenHeader());
            if (requestTokenHeader != null && requestTokenHeader.startsWith(jwtProperties.getTokenHead())) {
                jwtToken = requestTokenHeader.substring(jwtProperties.getTokenHead().length()).trim();
            }
        }
        
        // 3. 如果都没有token，返回null
        if (jwtToken == null) {
            log.debug("请求中没有找到token");
            return null;
        }
        
        try {
            // 解析JWT令牌获取用户信息
            String username = jwtKit.parseJwtToken(jwtToken).get("username", String.class);
            
            if (username == null) {
                log.debug("JWT令牌中未找到用户名");
                return null;
            }

            // 查询用户信息
            User user = userService.lambdaQuery()
                    .eq(User::getUserAccount, username)
                    .one();
            
            if (user == null) {
                log.warn("JWT令牌中的用户不存在: {}", username);
                return null;
            }

            // 设置Spring Security认证信息
            setSecurityContextAuthentication(user, request);
            
            log.debug("JWT认证成功，用户: {}", user.getUserName());
            return user;
            
        } catch (Exception e) {
            log.warn("JWT令牌解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 设置Spring Security认证上下文
     */
    private void setSecurityContextAuthentication(User user, HttpServletRequest request) {
        if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 创建认证令牌
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                            user.getUserAccount(),
                            null, 
                            null
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // 将认证信息存入Security上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    /**
     * 处理权限验证
     */
    private void handleAuthorization(LoginRequired loginRequired, AuthCheck authCheck, User currentUser, HttpServletRequest request) {
        // 如果两个注解都没有，检查是否为管理接口（自动识别）
        if (loginRequired == null && authCheck == null) {
            // 自动识别管理接口：以/add、/update、/delete开头的接口需要管理员权限
            String requestURI = request.getRequestURI();
            if (isManagementInterface(requestURI)) {
                if (currentUser == null) {
                    log.error("管理接口需要登录，但用户未登录");
                    throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
                }
                
                // 检查是否为管理员
                if (!UserConstant.ADMIN_ROLE.equals(currentUser.getUserRole())) {
                    log.error("权限验证失败：用户[{}]没有管理员权限", currentUser.getUserName());
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "权限不足，需要管理员权限");
                }
            }
            return;
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
    }
    
    /**
     * 判断是否为管理接口
     */
    private boolean isManagementInterface(String requestURI) {
        return requestURI.matches(".*/(add|update|delete)(/.*)?$") && 
               !requestURI.contains("/user/register") && 
               !requestURI.contains("/user/login");
    }
    
    /**
     * 从Cookie中获取token
     */
    private String getTokenFromCookie(HttpServletRequest request) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (token != null && !token.trim().isEmpty()) {
                        log.debug("从Cookie中获取到token");
                        return token.trim();
                    }
                }
            }
        }
        return null;
    }
}