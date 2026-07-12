package com.ldz.park.web.admin;

import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.model.request.LoginForm;
import com.ldz.park.model.vo.SimpleUser;
import com.ldz.park.service.AuthTokenService;
import com.ldz.park.service.UserService;
import com.ldz.park.util.JwtUtil;
import com.ldz.park.web.validator.user.UserLoginValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "后台管理 / 认证", description = "后台管理端登录、登出、强制下线")
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserLoginValidator userLoginValidator;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "账号密码登录")
    @PostMapping("/login")
    public ApiResponse<SimpleUser> login(@RequestBody LoginForm loginForm) {
        userLoginValidator.validate(loginForm);
        return ApiResponse.success(userService.login(loginForm.getAccount()));
    }

    @Operation(summary = "登出")
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

    @Operation(summary = "强制下线")
    @PostMapping("/force-logout")
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
