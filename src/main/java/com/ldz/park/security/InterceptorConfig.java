package com.ldz.park.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;

@Configuration
public class InterceptorConfig implements  WebMvcConfigurer  {

    @Autowired
    @Qualifier("userSecurityInterceptor")
    HandlerInterceptor userSecurityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userSecurityInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 新版认证白名单
                        "/api/wechat/auth/mini-login",
                        "/api/admin/auth/login",
                        // 小程序车位列表无需鉴权
                        "/api/wechat/market/list",
                        // 兼容旧接口（迁移期）
                        "/api/login",
                        "/api/wx-login",
                        "/api/oauth/mini/login",
                        "/api/register",
                        "/favicon.ico",

                        "/lib/**",
                        "/api/sso/**",
                        // swagger / openapi
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"

                );

    }

}
