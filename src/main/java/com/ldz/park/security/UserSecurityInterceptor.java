package com.ldz.park.security;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserSecurityInterceptor implements  HandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查用户是否登录（这里简单示范使用 session 存储登录状态）
        Object user = request.getSession().getAttribute("user");

        if (user == null) {
            // 未登录，重定向到登录页面
            response.sendRedirect("/login");
            return false; // 阻止请求继续执行
        }

        // 用户已登录，继续请求处理
        return true;
    }

}
