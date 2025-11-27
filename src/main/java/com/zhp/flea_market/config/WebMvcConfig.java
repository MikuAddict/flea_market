package com.zhp.flea_market.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AutomatedAuthInterceptor automatedAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册自动化认证拦截器
        registry.addInterceptor(automatedAuthInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns("/user/login", "/user/register", "/", "/swagger-ui/**", 
                        "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**", "/api-docs/**",
                        "/category/list", "/news/list", "/news/latest", "/news/detail/*"); // 排除这些路径，包括公共访问接口
    }
}