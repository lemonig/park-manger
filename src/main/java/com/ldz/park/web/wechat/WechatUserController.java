package com.ldz.park.web.wechat;

import com.ldz.park.model.User;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "微信小程序 / 用户", description = "小程序端登录用户个人资料")
@RestController
@RequestMapping("/api/wechat/user")
public class WechatUserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "获取当前登录用户资料")
    @GetMapping("/profile")
    public ApiResponse<User> profile(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("用户身份验证失败");
        }
        User user = userService.detail(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return ApiResponse.success(user);
    }

    @Operation(summary = "更新当前登录用户资料")
    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@RequestBody User body, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("用户身份验证失败");
        }
        if (body == null) {
            return ApiResponse.badRequest("请求体不能为空");
        }
        User patch = new User();
        patch.setId(userId);
        patch.setName(body.getName());
        patch.setAvatar(body.getAvatar());
        patch.setMobile(body.getMobile());
        patch.setDescription(body.getDescription());
        patch.setDoorplate(body.getDoorplate());
        userService.update(patch);
        return ApiResponse.ok();
    }
}
