package com.ldz.park.web.admin;

import com.ldz.park.model.User;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "后台管理 / 用户", description = "后台管理端用户管理")
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户列表")
    @GetMapping
    public ApiResponse<List<User>> list() {
        return userService.getListAll();
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    public ApiResponse<User> detail(@PathVariable Integer id) {
        User user = userService.detail(id);
        if (user != null) {
            user.setPassword(null);
        }
        return ApiResponse.success(user);
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public ApiResponse<Void> add(@RequestBody User user) {
        userService.add(user);
        return ApiResponse.ok();
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Integer id, @RequestBody User user) {
        user.setId(id);
        userService.update(user);
        return ApiResponse.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        userService.delete(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "重置密码")
    @PostMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Integer id) {
        userService.resetPassword(id);
        return ApiResponse.ok();
    }
}
