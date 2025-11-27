package com.zhp.flea_market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置类
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // 允许所有请求，权限验证由拦截器处理
                .anyRequest().permitAll()
            )
            // 禁用CSRF
            .csrf(csrf -> csrf.disable())
            // 禁用CORS
            .cors(cors -> cors.disable());

        return http.build();
    }
}