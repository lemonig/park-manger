package com.ldz.park.service;

import com.ldz.park.dao.UserMapper;
import com.ldz.park.model.SearchForm;
import com.ldz.park.model.User;
import com.ldz.park.model.domain.LogLogin;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.model.request.LoginForm;
import com.ldz.park.model.vo.SimpleUser;
import com.ldz.park.util.JwtUtil;
import com.ldz.park.util.TokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    public List<User> list() {
        return userMapper.getListAll();
    }

    // public ApiResponse getUserList(SearchForm form){
    // ApiResponse apiResponse= new ApiResponse();
    // List<User> list= userMapper.getUserList(form);
    // apiResponse.setData(list);
    // return apiResponse;
    // }

    public SimpleUser login(String account) {
        User user = userMapper.getUserByAccount(account);
        LogLogin log = new LogLogin();
        log.setUser_id(user.getId());
        log.setNickname(user.getName());
        log.setChannel(1);
        log.setType(1);
        // 生成 JWT token
        String token = jwtUtil.generateToken(user.getId());
        return new SimpleUser().fromUser(user, token);
    }

    // 已废弃，使用 JWT token 系统，不再需要此方法
    // private SimpleUser getUserLoginResponse(User user) {
    // String token = TokenHelper.getGUID();
    // userMapper.insertToken(user.getId(), token);
    // return new SimpleUser().fromUser(user, token);
    // }

    public ApiResponse getListAll() {
        List<User> list = userMapper.getListAll();
        System.out.println(list);
        return ApiResponse.success(list);
    }

    // public void deleteFailUser(String user_name) {
    // userMapper.deleteFailUser(user_name);
    // }

    // 已废弃，现在使用 JWT token 系统，不再通过数据库验证 token
    // 如需使用旧的 token 系统，请调用此方法
    @Deprecated
    public Integer getUserIdByToken(String token) {
        return userMapper.getUserIdByToken(token);
    }
}
