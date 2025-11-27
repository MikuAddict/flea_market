package com.zhp.flea_market.config;

import com.zhp.flea_market.annotation.AuthCheck;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Method;

/**
 * Web MVC配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                // 检查是否有AuthCheck注解
                if (handler instanceof org.springframework.web.method.HandlerMethod) {
                    org.springframework.web.method.HandlerMethod handlerMethod = (org.springframework.web.method.HandlerMethod) handler;
                    Method method = handlerMethod.getMethod();
                    AuthCheck authCheck = method.getAnnotation(AuthCheck.class);
                    
                    // 如果没有AuthCheck注解，或者注解的mustRole为空，不需要JWT验证
                    if (authCheck == null || authCheck.mustRole().isEmpty()) {
                        return true;
                    }
                    
                    // 有AuthCheck注解且指定了角色，需要JWT验证
                    return jwtInterceptor.preHandle(request, response, handler);
                }
                
                return true;
            }
        }).addPathPatterns("/**") // 拦截所有请求
        .excludePathPatterns("/user/login", "/user/register", "/", "/swagger-ui/**", 
                "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**", "/api-docs/**"); // 排除这些路径
    }
}