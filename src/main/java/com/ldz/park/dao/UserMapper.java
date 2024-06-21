package com.ldz.park.dao;

import com.ldz.park.model.SearchForm;
import com.ldz.park.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    List<User> getListAll();
//    List<User> getUserList(SearchForm form);

}
