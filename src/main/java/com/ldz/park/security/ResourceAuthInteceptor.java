package com.ldz.park.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
/**
 * 检查用户是否拥有菜单访问权限
 */
@Component
public class ResourceAuthInteceptor implements HandlerInterceptor {
    static final Logger logger = LoggerFactory.getLogger(ResourceAuthInteceptor.class);

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //每次请求要求客户端携带api请求所在的页面url
        /*Integer user_id = Integer.valueOf(request.getParameter("uid"));
        Map<String, Boolean> menuMap = (Map<String, Boolean>)request.getSession().getAttribute("menuMap");
        if(menuMap.containsKey(request.getRequestURI())){
            if(menuMap.get(request.getRequestURI())){
                return true;
            }
        }*/
        return true;
    }
}
