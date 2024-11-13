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
        return new ApiResponse(userService.login(loginForm.getAccount()));

    }

//    @PostMapping(value = "/user/list")
//    public ApiResponse useList (HttpServletRequest request, @RequestBody SearchForm form){
//        return userService.getUserList(form);
//    }

    @PostMapping(value = "/user/all")
    public ApiResponse userAllList (HttpServletRequest request){
        return userService.getListAll();
    }

//    @PostMapping(value = "user/add")
//    public ApiResponse userAdd (HttpServletRequest request,RequestBody ){
//        return userService.list();
//    }


}
