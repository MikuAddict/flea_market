package com.zhp.flea_market.config;

import com.zhp.flea_market.common.JwtKit;
import com.zhp.flea_market.common.JwtProperties;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtKit jwtKit;
    private final JwtProperties jwtProperties;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtKit jwtKit, UserService userService, JwtProperties jwtProperties) {
        this.jwtKit = jwtKit;
        this.jwtProperties = jwtProperties;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        // 从请求头中获取JWT令牌
        final String requestTokenHeader = request.getHeader(jwtProperties.getTokenHeader());
        
        if (requestTokenHeader != null && requestTokenHeader.startsWith(jwtProperties.getTokenHead())) {
            // 提取JWT令牌
            String jwtToken = requestTokenHeader.substring(jwtProperties.getTokenHead().length());
            try {
                // 解析JWT令牌
                String username = jwtKit.parseJwtToken(jwtToken).get("username", String.class);
                
                // 如果用户名不为空且当前没有认证信息
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 从数据库中获取用户信息
                    User user = userService.lambdaQuery()
                            .eq(User::getUserAccount, username)
                            .one();
                    
                    if (user != null) {
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
                        
                        // 将用户角色存入request属性，供AuthAspect使用
                        request.setAttribute("userRole", user.getUserRole());
                    }
                }
            } catch (Exception e) {
                logger.error("无法获取用户信息", e);
            }
        }
        
        // 继续执行过滤器链
        chain.doFilter(request, response);
    }
}