package com.ldz.park.web;

import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.model.request.LoginForm;
import com.ldz.park.model.vo.SimpleUser;
import com.ldz.park.service.AuthTokenService;
import com.ldz.park.service.UserService;
import com.ldz.park.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/oauth")
public class OAuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 小程序统一登录/续期接口。
     * - 请求体：{ code: 微信登录 code, token?: 旧 token }
     * - 返回：新 token + 用户信息
     */
    @PostMapping("/mini/login")
    public ApiResponse<SimpleUser> miniLogin(@RequestBody LoginForm loginForm) {
        if (loginForm == null || loginForm.getCode() == null || loginForm.getCode().isBlank()) {
            return ApiResponse.badRequest("微信登录凭证不能为空");
        }
        return ApiResponse.success(userService.miniLogin(loginForm));
    }

    /**
     * 主动登出：把当前 token 的 jti 加入黑名单。
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            return ApiResponse.ok();
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            String jti = jwtUtil.extractJti(token);
            long remain = jwtUtil.getRemainingSeconds(token);
            authTokenService.revokeJti(jti, remain);
        } catch (Exception ignored) {
        }
        return ApiResponse.ok();
    }

    /**
     * 后台强制下线：将某用户在指定 channel（默认 mini）下的当前 token 拉黑。
     */
    @PostMapping("/admin/force-logout")
    public ApiResponse<Void> forceLogout(@RequestBody ForceLogoutForm form) {
        if (form == null || form.getUserId() == null) {
            return ApiResponse.badRequest("用户ID不能为空");
        }
        String channel = form.getChannel() == null || form.getChannel().isBlank()
                ? JwtUtil.CHANNEL_MINI : form.getChannel();
        authTokenService.forceLogout(form.getUserId(), channel, jwtUtil.getMiniTtl());
        return ApiResponse.ok();
    }

    public static class ForceLogoutForm {
        private Integer userId;
        private String channel;

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }
    }
}
