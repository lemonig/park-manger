package com.ldz.park.dao;

import com.ldz.park.model.DictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DictTypeMapper {
    List<DictType> list();

    DictType detail(@Param("id") Long id);

    void add(@Param("dictType") DictType dictType);

    void update(@Param("dictType") DictType dictType);

    void delete(@Param("id") Long id);
}

