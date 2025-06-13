package com.ldz.park.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {
    private static final String SECRET = "this_is_a_very_long_256_bit_secret_key_change_me"; // 建议放入配置
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION_TIME = 3600_000; // 1 hour

    @Autowired
    private StringRedisTemplate redisTemplate;

    public String generateToken(String username) {
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
        redisTemplate.opsForValue().set("token:" + username, token, EXPIRATION_TIME, TimeUnit.MILLISECONDS);
        return token;
    }

    public String validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String username = claims.getSubject();
            String storedToken = redisTemplate.opsForValue().get("token:" + username);
            if (token.equals(storedToken) && !isTokenExpired(token)) {
                return username;
            }
        } catch (JwtException e) {
            // Token 无效或篡改
        }
        throw new RuntimeException("Invalid or expired token");
    }

    public void invalidateToken(String token) {
        try {
            String username = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            redisTemplate.delete("token:" + username);
            redisTemplate.opsForValue().set("blacklist:" + token, "invalid", EXPIRATION_TIME, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // 忽略解析异常
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
