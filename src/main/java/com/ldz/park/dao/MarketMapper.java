package com.ldz.park.dao;

import com.ldz.park.model.Market;
import com.ldz.park.model.vo.market.SimpleImage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MarketMapper {
    List<Market> list();

    void add(@Param("market") Market market);
    void update(@Param("market") Market market);
    void delete(Integer id);
    Market getDetailById(Integer id);

// insert image
    @Insert("insert into market_images (market_code, image_id, create_by) values (#{market_code}, #{image_id}, #{create_by})")
    int insertImg(@Param("market_code") String market_code,
                  @Param("image_id") String image_id,
                  @Param("create_by") Integer create_by);

    List<SimpleImage> selectImagesByMarketCode(@Param("marketCode") String marketCode);
}
