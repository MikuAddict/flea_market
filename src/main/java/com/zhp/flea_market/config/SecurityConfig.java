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
                // 允许无需认证访问的路径
                .requestMatchers("/").permitAll()
                .requestMatchers("/api/test/**").permitAll()
                .requestMatchers("/user/register").permitAll()
                .requestMatchers("/user/login").permitAll()
                .requestMatchers("/doc.html").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/knife4j/**").permitAll()
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            )
            // 禁用CSRF
            .csrf(csrf -> csrf.disable())
            // 禁用CORS
            .cors(cors -> cors.disable());

        return http.build();
    }
}