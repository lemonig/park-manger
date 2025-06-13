package com.ldz.park.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldz.park.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Component
public class UserSecurityInterceptor implements  HandlerInterceptor {

    @Autowired
    UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
            try {
                String username = JwtUtil.validateToken(token);
                if (!JwtUtil.isTokenExpired(token)) {
                    // Token 有效，可以将用户信息存入上下文（可选）
                    request.setAttribute("username", username);
                    return true;
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token expired");
                    return false;
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid token");
                return false;
            }
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Missing or invalid Authorization header");
        return false;
    }

    /**
     * 用户是否正常登录状态：token有效、用户存在、用户未被限制登录
     *
     * @param token
     * @return
     */
    private Boolean isUserAuthenticated(String token) {
        if (token == null) {
            return false;
        }

        Integer userId = userService.getUserIdByToken(token);
        if (userId == null) {
            return false;
        }

        User user = userService.getUserById(userId);
        if (!user.getIsActive()) {
            return false;
        }
        return true;
    }

}
