package com.ldz.park.web.validator.user;

import com.ldz.park.model.User;
import com.ldz.park.model.meta.ErrorCode;
import com.ldz.park.model.meta.ServerException;
import com.ldz.park.model.request.LoginForm;
import com.ldz.park.service.UserService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class UserLoginValidator {

    @Autowired
    UserService userService;

    public String validate(Object target) {
        LoginForm loginForm = (LoginForm) target;
        if (loginForm.getAccount() == null || loginForm.getPassword() == null ||
                loginForm.getAccount().trim().length() == 0 ||
                loginForm.getPassword().trim().length() == 0) {
            throw new ServerException(ErrorCode.FIELD_ERROR.getCode(), "请输入正确的登录账号或密码");
        }

        /** 15分钟内可有锁定的用户 */
        // Boolean lock = userService.userlock(loginForm.getAccount());
        // if(lock != null && lock) {
        // throw new ServerException(ErrorCode.FIELD_ERROR.getCode(), "用户已锁定,请稍后再试");
        // }

        List<User> userList = userService.list();
        for (User user : userList) {
            String account = user.getAccount();
            String password = user.getPassword();

            /** 匹配成功则返回 */
            if (account.equals(loginForm.getAccount()) && password.equals(loginForm.getPassword())) {
                loginForm.setAccount(user.getAccount());
//                userService.deleteFailUser(loginForm.getAccount());
                return null;
            }
        }
        // 添加默认返回值，表示验证失败
        throw new ServerException(ErrorCode.FIELD_ERROR.getCode(), "用户名或密码错误");
    }

}
