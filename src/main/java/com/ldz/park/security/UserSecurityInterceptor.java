package com.ldz.park.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldz.park.model.meta.ApiErrorResponse;
import com.ldz.park.model.meta.ErrorCode;
import com.ldz.park.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Component
public class UserSecurityInterceptor implements  HandlerInterceptor {

    @Autowired
    UserService userService;



    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        if(isUserAuthenticated(token)){
            return true;
        }else {
            response.setHeader("content-type", "application/json; charset=utf-8");
            PrintWriter out = response.getWriter();
            ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
            apiErrorResponse.setError(ErrorCode.UNAUTHENTICATED.getCode());
            apiErrorResponse.setMessage("用户未认证或token过期，请重新登录后继续");
            apiErrorResponse.setPath(request.getServletPath());
            String errorMsg = new ObjectMapper().writeValueAsString(apiErrorResponse);
            out.write(errorMsg);
            return false;
        }
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

//        User user = userService.getUserById(userId);
//        if (!user.getIsActive()) {
//            return false;
//        }
        return true;
    }

}
