package com.ldz.park.service;

import com.ldz.park.dao.UserMapper;
import com.ldz.park.model.SearchForm;
import com.ldz.park.model.User;
import com.ldz.park.model.meta.ApiResponse;
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

    public ApiResponse getListAll(){
        List<User> list = userMapper.getListAll();
        System.out.println(list);
        return new ApiResponse(list);
    }
}
