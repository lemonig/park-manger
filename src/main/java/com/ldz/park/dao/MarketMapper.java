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
    List<Market> list(Market query);

    void add(Market market);

    int update(Market market);

    int delete(Long id);

    Market getDetailById(Long id);

    List<SimpleImage> selectImagesByMarketCodes(@Param("codes") List<String> codes);

    List<SimpleImage> selectImagesByMarketCode(@Param("marketCode") String marketCode);

    int insertImagesBatch(@Param("marketCode") String marketCode,
                          @Param("imageIds") List<String> imageIds,
                          @Param("userId") Integer userId);

    int insertImg(@Param("market_code") String market_code,
                  @Param("image_id") String image_id,
                  @Param("create_by") Integer create_by);

    int deleteImagesByMarketCode(@Param("marketCode") String marketCode);
}