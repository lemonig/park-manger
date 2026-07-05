package com.ldz.park.util;

import com.ldz.park.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {

    public static final String CHANNEL_MINI = "mini";
    public static final String CHANNEL_ADMIN = "admin";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400}")
    private long expiration;

    @Value("${jwt.mini.ttl:2592000}")
    private long miniTtl;

    @Value("${jwt.mini.refresh-threshold:604800}")
    private long miniRefreshThreshold;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public long getMiniTtl() {
        return miniTtl;
    }

    public long getMiniRefreshThreshold() {
        return miniRefreshThreshold;
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Integer extractUserId(String token) {
        Claims claims = parseClaims(token);
        return Integer.valueOf(claims.getSubject());
    }

    public String extractChannel(String token) {
        Object channel = parseClaims(token).get("channel");
        return channel == null ? null : channel.toString();
    }

    public String extractJti(String token) {
        return parseClaims(token).getId();
    }

    public String extractOpenid(String token) {
        Object openid = parseClaims(token).get("openid");
        return openid == null ? null : openid.toString();
    }

    public boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    public long getRemainingSeconds(String token) {
        Date exp = parseClaims(token).getExpiration();
        long remainMs = exp.getTime() - System.currentTimeMillis();
        return remainMs > 0 ? remainMs / 1000 : 0;
    }

    /**
     * 生成 JWT token（旧接口，后台账号登录默认走 admin channel）
     */
    public String generateToken(Integer userId) {
        User user = new User();
        user.setId(userId);
        user.setRole("admin");
        return generateToken(user, CHANNEL_ADMIN, expiration, UUID.randomUUID().toString());
    }

    public String generateMiniToken(User user, String jti) {
        return generateToken(user, CHANNEL_MINI, miniTtl, jti);
    }

    public String generateToken(User user, String channel, long ttlSeconds, String jti) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ttlSeconds * 1000L);

        Map<String, Object> claims = new HashMap<>();
        claims.put("channel", channel);
        if (user.getOpenid() != null) {
            claims.put("openid", user.getOpenid());
        }
        if (user.getRole() != null) {
            claims.put("role", user.getRole());
        }

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .id(jti)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
}
