package com.ldz.park.service;

import com.ldz.park.model.User;
import com.ldz.park.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 处理小程序/后台 JWT 的签发、续期、黑名单与单点登录控制。
 */
@Service
public class AuthTokenService {

    private static final String KEY_MINI_TOKEN_PREFIX = "auth:token:mini:";
    private static final String KEY_ADMIN_TOKEN_PREFIX = "auth:token:admin:";
    private static final String KEY_BLACKLIST_PREFIX = "auth:blacklist:";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    /**
     * 为小程序用户签发新 token，并把当前 jti 记录到 Redis 用于单点登录。
     */
    public String issueMiniToken(User user) {
        String jti = UUID.randomUUID().toString();
        String token = jwtUtil.generateMiniToken(user, jti);
        recordActiveJti(KEY_MINI_TOKEN_PREFIX + user.getId(), jti, jwtUtil.getMiniTtl());
        return token;
    }

    /**
     * 后台管理端签发 token（沿用旧逻辑，主要用于兼容）。
     */
    public String issueAdminToken(User user) {
        String jti = UUID.randomUUID().toString();
        String token = jwtUtil.generateToken(user, JwtUtil.CHANNEL_ADMIN, 86400L, jti);
        recordActiveJti(KEY_ADMIN_TOKEN_PREFIX + user.getId(), jti, 86400L);
        return token;
    }

    private void recordActiveJti(String key, String jti, long ttlSeconds) {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, jti, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
    }

    /**
     * 判断 jti 是否为该用户在指定 channel 下的当前有效 token。
     * Redis 不可用或未记录时默认放行（fail-open），避免影响业务。
     */
    public boolean isCurrentToken(Integer userId, String channel, String jti) {
        if (redisTemplate == null || jti == null) {
            return true;
        }
        try {
            String key = keyForChannel(channel) + userId;
            String current = redisTemplate.opsForValue().get(key);
            if (current == null) {
                return true;
            }
            return current.equals(jti);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 判断 token 是否已加入黑名单。
     */
    public boolean isBlacklisted(String jti) {
        if (redisTemplate == null || jti == null) {
            return false;
        }
        try {
            Boolean has = redisTemplate.hasKey(KEY_BLACKLIST_PREFIX + jti);
            return Boolean.TRUE.equals(has);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 主动登出：把 jti 写入黑名单直到原过期时间。
     */
    public void revokeJti(String jti, long remainingSeconds) {
        if (redisTemplate == null || jti == null || remainingSeconds <= 0) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(KEY_BLACKLIST_PREFIX + jti, "1", remainingSeconds, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
    }

    /**
     * 强制下线某个用户：拉黑该 channel 下当前 jti，并清理记录。
     */
    public void forceLogout(Integer userId, String channel, long fallbackTtl) {
        if (redisTemplate == null) {
            return;
        }
        try {
            String key = keyForChannel(channel) + userId;
            String jti = redisTemplate.opsForValue().get(key);
            if (jti != null) {
                revokeJti(jti, fallbackTtl);
                redisTemplate.delete(key);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 登录时如有旧 token 且仍有效，把旧 jti 拉黑，避免并存。
     */
    public void revokeIfPresent(String oldToken) {
        if (oldToken == null || oldToken.isBlank()) {
            return;
        }
        try {
            String jti = jwtUtil.extractJti(oldToken);
            long remain = jwtUtil.getRemainingSeconds(oldToken);
            if (jti != null && remain > 0) {
                revokeJti(jti, remain);
            }
        } catch (Exception ignored) {
        }
    }

    private String keyForChannel(String channel) {
        if (JwtUtil.CHANNEL_ADMIN.equals(channel)) {
            return KEY_ADMIN_TOKEN_PREFIX;
        }
        return KEY_MINI_TOKEN_PREFIX;
    }
}
