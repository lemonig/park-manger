package com.ldz.park.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldz.park.model.meta.ApiErrorResponse;
import com.ldz.park.model.meta.ErrorCode;
import com.ldz.park.service.UserService;
import com.ldz.park.util.JwtUtil;
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
    private ObjectMapper objectMapper; // ✅ 注入 Spring 管理的 ObjectMapper

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        // 从标准的 Authorization header 获取 token，支持 Bearer 前缀
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null) {
            if (authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7); // 去掉 "Bearer " 前缀
            } else {
                token = authHeader; // 兼容没有 Bearer 前缀的情况
            }
        }

        if (token == null || token.trim().isEmpty()) {
            return sendUnauthorizedResponse(request, response, "未授权访问");
        }
        if (jwtUtil.isTokenExpired(token)) {
            return sendUnauthorizedResponse(request, response, "登录已过期，请重新登录");
        }

        Integer userId = jwtUtil.extractUserId(token);
        if (userId == null) {
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
