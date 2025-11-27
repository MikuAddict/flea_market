package com.zhp.flea_market.config;

import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.common.JwtKit;
import com.zhp.flea_market.common.JwtProperties;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.service.UserService;
import com.zhp.flea_market.constant.UserConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtKit jwtKit;
    private final JwtProperties jwtProperties;
    private final UserService userService;

    public JwtInterceptor(JwtKit jwtKit, JwtProperties jwtProperties, UserService userService) {
        this.jwtKit = jwtKit;
        this.jwtProperties = jwtProperties;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("JWT拦截器开始工作...");
        
        // 放行OPTIONS请求
        if ("OPTIONS".equals(request.getMethod())) {
            log.info("放行OPTIONS请求");
            return true;
        }

        log.info("请求头名称: {}", jwtProperties.getTokenHeader());
        log.info("令牌前缀: {}", jwtProperties.getTokenHead());
        
        // 从请求头中获取JWT令牌
        String tokenHeader = request.getHeader(jwtProperties.getTokenHeader());
        log.info("完整token: {}", tokenHeader);
        
        if (tokenHeader == null || !tokenHeader.startsWith(jwtProperties.getTokenHead())) {
            log.error("请求头中没有token或token格式不正确");
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 提取JWT令牌
        String token = tokenHeader.substring(jwtProperties.getTokenHead().length());
        // 去除可能的空格
        token = token.trim();
        log.info("提取的token: {}", token);
        
        try {
            // 解析JWT令牌获取用户信息
            log.info("开始解析token...");
            String username = jwtKit.parseJwtToken(token).get("username", String.class);
            log.info("解析token成功，用户名: {}", username);
            
            // 查询用户信息
            log.info("查询用户信息...");
            User user = userService.lambdaQuery()
                    .eq(User::getUserAccount, username)
                    .one();
            
            if (user == null) {
                log.error("用户不存在: {}", username);
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            }
            
            log.info("查询用户成功: {}", user.getUserName());
            
            // 将用户信息存入请求属性
            request.setAttribute("currentUser", user);
            request.setAttribute("userRole", user.getUserRole());
            
            return true;
        } catch (Exception e) {
            log.error("token解析失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "token无效或已过期");
        }
    }
}