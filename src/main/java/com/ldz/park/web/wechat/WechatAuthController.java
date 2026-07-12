package com.ldz.park.web.wechat;

import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.model.request.LoginForm;
import com.ldz.park.model.vo.SimpleUser;
import com.ldz.park.service.AuthTokenService;
import com.ldz.park.service.UserService;
import com.ldz.park.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "微信小程序 / 认证", description = "小程序端登录、续期、登出")
@RestController
@RequestMapping("/api/wechat/auth")
public class WechatAuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "微信小程序静默登录/续期")
    @PostMapping("/mini-login")
    public ApiResponse<SimpleUser> miniLogin(@RequestBody LoginForm loginForm) {
        if (loginForm == null || loginForm.getCode() == null || loginForm.getCode().isBlank()) {
            return ApiResponse.badRequest("微信登录凭证不能为空");
        }
        return ApiResponse.success(userService.miniLogin(loginForm));
    }

    @Operation(summary = "小程序主动登出")
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
}
