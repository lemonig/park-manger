package com.ldz.park.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")  // application.yml 加 jwt.secret=your_very_long_secret_key (至少256位)
    private String secret;

    @Value("${jwt.expiration:86400}")  // 默认24小时过期，单位秒
    private long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 从 token 解析用户ID
     */
    public Integer extractUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Integer.valueOf(claims.getSubject());  // 假设 subject 是 userId
    }

    /**
     * 校验 token 是否过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration().before(new Date());
    }

    /**
     * 生成 JWT token
     */
    public String generateToken(Integer userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + TimeUnit.SECONDS.toMillis(expiration));

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
}