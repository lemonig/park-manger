package com.ldz.park.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldz.park.model.User;
import com.ldz.park.model.meta.ApiErrorResponse;
import com.ldz.park.model.meta.ErrorCode;
import com.ldz.park.service.AuthTokenService;
import com.ldz.park.service.UserService;
import com.ldz.park.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

@Component
public class UserSecurityInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null) {
            if (authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else {
                token = authHeader;
            }
        }

        if (token == null || token.trim().isEmpty()) {
            return sendUnauthorizedResponse(request, response, "未授权访问");
        }
        try {
            Claims claims = jwtUtil.parseClaims(token);
            if (claims.getExpiration().before(new java.util.Date())) {
                return sendUnauthorizedResponse(request, response, "登录已过期，请重新登录");
            }

            Integer userId;
            try {
                userId = Integer.valueOf(claims.getSubject());
            } catch (Exception e) {
                return sendUnauthorizedResponse(request, response, "用户身份验证失败");
            }
            String jti = claims.getId();
            Object channelObj = claims.get("channel");
            String channel = channelObj == null ? JwtUtil.CHANNEL_ADMIN : channelObj.toString();
            Object openidObj = claims.get("openid");

            // 黑名单校验
            if (authTokenService.isBlacklisted(jti)) {
                return sendUnauthorizedResponse(request, response, "登录已失效，请重新登录");
            }
            // 单点登录校验
            if (!authTokenService.isCurrentToken(userId, channel, jti)) {
                return sendUnauthorizedResponse(request, response, "账号已在别处登录");
            }

            request.setAttribute("userId", userId);
            request.setAttribute("channel", channel);
            if (openidObj != null) {
                request.setAttribute("openid", openidObj.toString());
            }

            // 小程序自动续期：剩余时长低于阈值时签发新 token 通过响应头返回
            if (JwtUtil.CHANNEL_MINI.equals(channel)) {
                long remain = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
                if (remain > 0 && remain < jwtUtil.getMiniRefreshThreshold()) {
                    User user = userService.detail(userId);
                    if (user != null) {
                        if (openidObj != null && (user.getOpenid() == null || user.getOpenid().isBlank())) {
                            user.setOpenid(openidObj.toString());
                        }
                        if (user.getRole() == null || user.getRole().isBlank()) {
                            user.setRole("user");
                        }
                        String newToken = authTokenService.issueMiniToken(user);
                        authTokenService.revokeJti(jti, remain);
                        response.setHeader("X-New-Token", newToken);
                        response.setHeader("Access-Control-Expose-Headers", "X-New-Token");
                    }
                }
            }
        } catch (ExpiredJwtException e) {
            return sendUnauthorizedResponse(request, response, "登录已过期，请重新登录");
        } catch (JwtException | IllegalArgumentException e) {
            return sendUnauthorizedResponse(request, response, "用户身份验证失败");
        }

        return true;
    }

    /**
     * 发送未授权响应
     */
    private boolean sendUnauthorizedResponse(HttpServletRequest request, HttpServletResponse response, String message)
            throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
        apiErrorResponse.setError(ErrorCode.UNAUTHENTICATED.getCode());
        apiErrorResponse.setMessage(message);
        apiErrorResponse.setPath(request.getServletPath());

        PrintWriter out = response.getWriter();
        out.write(objectMapper.writeValueAsString(apiErrorResponse));
        out.flush();

        return false;
    }
}
