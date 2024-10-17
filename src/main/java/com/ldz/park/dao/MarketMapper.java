package com.ldz.park.dao;

import com.ldz.park.model.Market;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MarketMapper {
    List<Market> list();

    void add(@Param("market") Market market);
    void update(@Param("market") Market market);
    void delete(Integer id);

}
