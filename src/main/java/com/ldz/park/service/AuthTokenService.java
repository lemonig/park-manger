package com.ldz.park.service;

import com.ldz.park.model.User;
import com.ldz.park.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理小程序/后台 JWT 的签发、续期与黑名单。
 * <p>
 * 纯 JWT 无状态方案，不依赖 Redis：
 * <ul>
 *   <li>黑名单使用进程内内存 {@link ConcurrentHashMap}，key 为 token 的 jti，value 为过期时间戳(ms)；</li>
 *   <li>单点互踢通过「新登录签发新 token 并把旧 jti 拉黑」实现；</li>
 *   <li>强制下线通过「用户+channel → 当前 jti」内存映射定位并拉黑实现；</li>
 *   <li>内存黑名单在应用重启后会清空，已登出/被顶的 token 在自身 exp 到期前可重新使用，对个人小程序可接受。</li>
 * </ul>
 */
@Service
public class AuthTokenService {

    private static final String KEY_MINI_TOKEN_PREFIX = "auth:token:mini:";
    private static final String KEY_ADMIN_TOKEN_PREFIX = "auth:token:admin:";

    /**
     * jti → 过期时间戳(ms) 的内存黑名单。
     */
    private final ConcurrentHashMap<String, Long> jtiBlacklist = new ConcurrentHashMap<>();

    /**
     * 用户+channel → 当前有效 jti，用于强制下线时定位并拉黑。
     */
    private final ConcurrentHashMap<String, String> activeJtiByUser = new ConcurrentHashMap<>();

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 为小程序用户签发新 token，并记录该用户当前 jti，便于强制下线/单点互踢。
     */
    public String issueMiniToken(User user) {
        String jti = UUID.randomUUID().toString();
        recordActiveJti(user.getId(), JwtUtil.CHANNEL_MINI, jti);
        return jwtUtil.generateMiniToken(user, jti);
    }

    /**
     * 后台管理端签发 token（沿用旧逻辑，主要用于兼容）。
     */
    public String issueAdminToken(User user) {
        String jti = UUID.randomUUID().toString();
        recordActiveJti(user.getId(), JwtUtil.CHANNEL_ADMIN, jti);
        return jwtUtil.generateToken(user, JwtUtil.CHANNEL_ADMIN, 86400L, jti);
    }

    private void recordActiveJti(Integer userId, String channel, String jti) {
        if (userId == null) {
            return;
        }
        String key = keyForChannel(channel) + userId;
        String oldJti = activeJtiByUser.put(key, jti);
        if (oldJti != null && !oldJti.equals(jti)) {
            // 同一用户新登录，作废旧 token 的 jti（剩余时长由调用方另行拉黑时也可指定）
            Long oldExpire = jtiBlacklist.get(oldJti);
            if (oldExpire == null) {
                // 未知过期时间时按 mini 通道默认 TTL 拉黑，避免旧 token 长期有效
                jtiBlacklist.put(oldJti, System.currentTimeMillis() + jwtUtil.getMiniTtl() * 1000L);
            }
        }
    }

    /**
     * 判断 token 的 jti 是否在内存黑名单中。
     */
    public boolean isBlacklisted(String jti) {
        if (jti == null) {
            return false;
        }
        Long expireAt = jtiBlacklist.get(jti);
        if (expireAt == null) {
            return false;
        }
        if (expireAt <= System.currentTimeMillis()) {
            jtiBlacklist.remove(jti);
            return false;
        }
        return true;
    }

    /**
     * 主动登出：把 jti 写入黑名单直到原 token 过期时间。
     */
    public void revokeJti(String jti, long remainingSeconds) {
        if (jti == null || remainingSeconds <= 0) {
            return;
        }
        long expireAt = System.currentTimeMillis() + remainingSeconds * 1000L;
        jtiBlacklist.put(jti, expireAt);
    }

    /**
     * 登录时如有旧 token 且仍有效，把旧 jti 拉黑，避免多 token 并存。
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

    /**
     * 强制下线某用户指定通道：定位其当前 jti 并拉黑。
     */
    public void forceLogout(Integer userId, String channel, long fallbackTtl) {
        if (userId == null) {
            return;
        }
        String key = keyForChannel(channel) + userId;
        String jti = activeJtiByUser.remove(key);
        if (jti != null) {
            revokeJti(jti, fallbackTtl);
        }
    }

    /**
     * 清理已过期的黑名单记录（可选，防内存增长；由调用方按需触发）。
     */
    public void purgeExpired() {
        long now = System.currentTimeMillis();
        jtiBlacklist.entrySet().removeIf(e -> e.getValue() <= now);
    }

    private String keyForChannel(String channel) {
        if (JwtUtil.CHANNEL_ADMIN.equals(channel)) {
            return KEY_ADMIN_TOKEN_PREFIX;
        }
        return KEY_MINI_TOKEN_PREFIX;
    }
}
