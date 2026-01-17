package com.ldz.park.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldz.park.model.meta.ApiErrorResponse;
import com.ldz.park.model.meta.ErrorCode;
import com.ldz.park.service.UserService;
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
    private ObjectMapper objectMapper; // ✅ 注入 Spring 管理的 ObjectMapper

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        String token = request.getHeader("token");
        if (isUserAuthenticated(token)) {
            return true;
        }

        // ❗ 未认证，直接返回 JSON
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
        apiErrorResponse.setError(ErrorCode.UNAUTHENTICATED.getCode());
        apiErrorResponse.setMessage("用户未认证或token过期，请重新登录后继续");
        apiErrorResponse.setPath(request.getServletPath());

        PrintWriter out = response.getWriter();
        out.write(objectMapper.writeValueAsString(apiErrorResponse));
        out.flush();

        return false;
    }

    /**
     * 用户是否正常登录状态
     */
    private boolean isUserAuthenticated(String token) {
        if (token == null) {
            return false;
        }

        Integer userId = userService.getUserIdByToken(token);
        return userId != null;
    }
}
