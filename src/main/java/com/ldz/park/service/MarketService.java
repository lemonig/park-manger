package com.ldz.park.service;

import com.github.pagehelper.PageInfo;
import com.ldz.park.dao.MarketMapper;
import com.ldz.park.entity.FileRecord;
import com.ldz.park.model.Market;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.model.vo.market.SimpleImage;
import com.ldz.park.util.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MarketService {
    @Autowired
    private MarketMapper marketMapper;

    @Autowired
    private FileRecordService fileRecordService;

    public ApiResponse list (){
        List<Market> list = marketMapper.list();
        for (Market m : list) {
            List<SimpleImage> image = marketMapper.selectImagesByMarketCode(m.getCode());
            m.setImages(image);
        }


        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setData(list);
        apiResponse.setAdditional_data(PaginationUtil.getTotal(new PageInfo<>(list)));
        return apiResponse;
    }

    public void add (Market market){
        marketMapper.add(market);
    }

    public void update(Market market){
        marketMapper.update(market);
    }

    public void delete(Integer id){
        marketMapper.delete(id);
    }

    public ApiResponse detail(Integer id){
        Market market = marketMapper.getDetailById(id);
        List<SimpleImage> imgs = marketMapper.selectImagesByMarketCode(market.getCode());
        market.setImages(imgs);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setData(market);
        return apiResponse;
    }

    public void insertImg(String marketCode,String image_id, Integer  userId){
        marketMapper.insertImg(marketCode,image_id,userId );
    }

    public List<SimpleImage> getImagesByMarketCode(String marketCode) {
        return marketMapper.selectImagesByMarketCode(marketCode);
    }
}
