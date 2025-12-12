package com.ldz.park.dao;

import com.ldz.park.model.DictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DictItemMapper {
    List<DictItem> list(@Param("typeCode") String typeCode);

    DictItem detail(@Param("id") Long id);

    void add(@Param("dictItem") DictItem dictItem);

    void update(@Param("dictItem") DictItem dictItem);

    void delete(@Param("id") Long id);

    void deleteByTypeCode(@Param("typeCode") String typeCode);
}

