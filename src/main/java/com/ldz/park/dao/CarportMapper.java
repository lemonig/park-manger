package com.ldz.park.dao;

import com.ldz.park.model.Carport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CarportMapper {
    List<Carport> list();

    void add(@Param("carport") Carport carport);
    void update(@Param("carport") Carport carport);
    void delete(Integer id);
    Carport detail(@Param("id") Integer id);
}
