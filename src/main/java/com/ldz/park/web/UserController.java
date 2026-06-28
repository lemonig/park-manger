package com.ldz.park.web;

import com.ldz.park.model.SearchForm;
import com.ldz.park.model.User;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.model.request.LoginForm;
import com.ldz.park.service.UserService;
import com.ldz.park.web.validator.user.UserLoginValidator;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api")
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    UserLoginValidator userLoginValidator;
 

    @PostMapping("/login")
    public ApiResponse login(@RequestBody LoginForm loginForm){
        userLoginValidator.validate(loginForm);
        return  ApiResponse.success(userService.login(loginForm.getAccount()));

    }

    @PostMapping("/wx-login")
    public ApiResponse wxLogin(@RequestBody LoginForm loginForm){
        if (loginForm.getCode() == null || loginForm.getCode().isBlank()) {
            return ApiResponse.badRequest("微信登录凭证不能为空");
        }
        return ApiResponse.success(userService.wxLogin(loginForm));
    }

//    @PostMapping(value = "/user/list")
//    public ApiResponse useList (HttpServletRequest request, @RequestBody SearchForm form){
//        return userService.getUserList(form);
//    }

    @PostMapping(value = "/user/all")
    public ApiResponse userAllList (HttpServletRequest request){
        return userService.getListAll();
    }

    @PostMapping(value = "/user/detail")
    public ApiResponse userDetail(@RequestBody User user) {
        return ApiResponse.success(userService.detail(user.getId()));
    }

    @PostMapping(value = "/user/add")
    public ApiResponse userAdd(@RequestBody User user) {
        userService.add(user);
        return ApiResponse.ok();
    }

    @PostMapping(value = "/user/update")
    public ApiResponse userUpdate(@RequestBody User user) {
        userService.update(user);
        return ApiResponse.ok();
    }

    @PostMapping(value = "/user/delete")
    public ApiResponse userDelete(@RequestBody User user) {
        userService.delete(user.getId());
        return ApiResponse.ok();
    }

    @PostMapping(value = "/user/reset-password")
    public ApiResponse userResetPassword(@RequestBody User user) {
        userService.resetPassword(user.getId());
        return ApiResponse.ok();
    }


}
