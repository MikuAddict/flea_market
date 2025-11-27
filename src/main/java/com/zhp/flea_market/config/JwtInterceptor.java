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
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

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
        // 放行OPTIONS请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 从请求头中获取JWT令牌
        String tokenHeader = request.getHeader(jwtProperties.getTokenHeader());
        if (tokenHeader == null || !tokenHeader.startsWith(jwtProperties.getTokenHead())) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 提取JWT令牌
        String token = tokenHeader.substring(jwtProperties.getTokenHead().length());
        
        try {
            // 解析JWT令牌获取用户信息
            String username = jwtKit.parseJwtToken(token).get("username", String.class);
            
            // 查询用户信息
            User user = userService.lambdaQuery()
                    .eq(User::getUserAccount, username)
                    .one();
            
            if (user == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            }
            
            // 将用户信息存入请求属性
            request.setAttribute("currentUser", user);
            request.setAttribute("userRole", user.getUserRole());
            
            return true;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "token无效或已过期");
        }
    }
}