package com.ldz.park.service;

import com.ldz.park.dao.UserMapper;
import com.ldz.park.model.SearchForm;
import com.ldz.park.model.User;
import com.ldz.park.model.domain.LogLogin;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.model.request.LoginForm;
import com.ldz.park.model.vo.SimpleUser;
import com.ldz.park.util.TokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

//    public ApiResponse getUserList(SearchForm form){
//        ApiResponse apiResponse= new ApiResponse();
//        List<User> list= userMapper.getUserList(form);
//        apiResponse.setData(list);
//        return apiResponse;
//    }


    public SimpleUser login(String account) {
        User user = userMapper.getUserByAccount(account);
        LogLogin log = new LogLogin();
        log.setUser_id(user.getId());
        log.setNickname(user.getName());
        log.setChannel(1);
        log.setType(1);
        String token = TokenHelper.getGUID();
        userMapper.insertToken(user.getId(), token);
        return getUserLoginResponse(user);
    }

    private SimpleUser getUserLoginResponse(User user){
        String token = TokenHelper.getGUID();
        userMapper.insertToken(user.getId(),token);
        return new SimpleUser().fromUser(user, token);
    }
    

    public ApiResponse getListAll(){
        List<User> list = userMapper.getListAll();
        System.out.println(list);
        return new ApiResponse(list);
    }


}
