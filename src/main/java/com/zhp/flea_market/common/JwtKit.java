package com.zhp.flea_market.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * JWT工具类
 *
 */
@Component
public class JwtKit {
    @Resource
    private JwtProperties jwtProperties;
    
    private SecretKey getSecretKey() {
        // 确保密钥长度至少为256位（32字节）
        String secret = jwtProperties.getSecret();
        if (secret.length() < 32) {
            // 如果密钥太短，使用安全的方式生成足够长的密钥
            return Jwts.SIG.HS256.key().build();
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成Token
     *
     * @param  user 自定义要存储的用户对象信息
     * @return string(Token)
     */
    public  <T> String generateToken(T user) {
        Map<String, Object> claims = new HashMap<String, Object>(10);
        
        // 根据用户对象类型存储正确的用户名
        if (user instanceof com.zhp.flea_market.model.entity.User) {
            com.zhp.flea_market.model.entity.User userEntity = (com.zhp.flea_market.model.entity.User) user;
            claims.put("username", userEntity.getUserAccount());
            claims.put("userId", userEntity.getId());
        } else if (user instanceof com.zhp.flea_market.model.vo.LoginUserVO) {
            com.zhp.flea_market.model.vo.LoginUserVO loginUserVO = (com.zhp.flea_market.model.vo.LoginUserVO) user;
            claims.put("username", loginUserVO.getUserAccount());
            claims.put("userId", loginUserVO.getId());
        } else {
            claims.put("username", user.toString());
        }
        
        claims.put("createdate", new Date());
        claims.put("id", System.currentTimeMillis());
        
        SecretKey key = getSecretKey();
        
        // 要存储的数据
        return Jwts.builder()
                .claims(claims)
                // 过期时间
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                // 加密算法和密钥
                .signWith(key)
                .compact(); // 打包返回
    }

    public JwtKit() {
    }

    public JwtKit(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * 校验Token是否合法
     *
     * @param token 要校验的Token
     * @return Claims (过期时间，用户信息，创建时间)
     */
    public Claims parseJwtToken(String token) {
        // 确保token不为空
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }
        
        // 去除可能的空格
        token = token.trim();
        
        SecretKey key = getSecretKey();
        
        JwtParser parser = Jwts.parser()
                .verifyWith(key)
                .build();
                
        return parser.parseSignedClaims(token).getPayload();
    }
}
