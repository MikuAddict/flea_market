package com.zhp.flea_market.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private ImageStorageConfig storageConfig;

    @Autowired
    private UnifiedAuthInterceptor unifiedAuthInterceptor;

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(unifiedAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login", "/user/register", "/swagger-ui/**", "/v3/api-docs/**");
    }

    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射图片访问路径到本地存储目录
        // 使用绝对路径确保正确映射
        String basePath = storageConfig.getBasePath();
        if (basePath.startsWith("./")) {
            basePath = basePath.substring(2); // 移除开头的"./"
        }
        
        String absolutePath = System.getProperty("user.dir") + "/" + basePath;
        
        registry.addResourceHandler(storageConfig.getBaseUrl() + "/**")
                .addResourceLocations("file:" + absolutePath + "/");
        
        // 配置静态资源缓存
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600); // 1小时缓存
    }
}