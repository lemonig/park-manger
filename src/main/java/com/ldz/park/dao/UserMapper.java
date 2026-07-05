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

    User getUserByOpenid(@Param("openid") String openid);

    User getUserByMobile(@Param("mobile") String mobile);

    User detail(@Param("id") Integer id);

    void add(@Param("user") User user);

    void update(@Param("user") User user);

    void delete(@Param("id") Integer id);

    void resetPassword(@Param("id") Integer id, @Param("password") String password);

    void insertToken(@Param("userId") Integer userId, @Param("token") String token);

//    void deleteFailUser(@Param("user_name") String user_name);

    Integer getUserIdByToken(String token);
}
