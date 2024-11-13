package com.ldz.park.web.validator.user;

import com.ldz.park.model.meta.ErrorCode;
import com.ldz.park.model.meta.ServerException;
import com.ldz.park.model.request.LoginForm;
import com.ldz.park.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class UserLoginValidator {

    @Autowired
    UserService userService;

    public void validate(Object target){
        LoginForm loginForm = (LoginForm) target;
        if(loginForm.getAccount() == null || loginForm.getPassword() == null ||
                loginForm.getAccount().trim().length() == 0 ||
                loginForm.getPassword().trim().length() == 0    ){
            throw new ServerException(ErrorCode.FIELD_ERROR.getCode(),"请输入正确的登录账号或密码");
        }
    }

}
