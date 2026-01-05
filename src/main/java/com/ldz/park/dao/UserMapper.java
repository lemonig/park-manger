package com.ldz.park.dao;

import com.ldz.park.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    List<User> getListAll();
    // List<User> getUserList(SearchForm form);

    User getUserByAccount(String account);

    void insertToken(@Param("userId") Integer userId, @Param("token") String token);

//    void deleteFailUser(@Param("user_name") String user_name);

    Integer getUserIdByToken(String token);
}
