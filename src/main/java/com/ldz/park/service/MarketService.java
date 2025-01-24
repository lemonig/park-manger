package com.ldz.park.service;

import com.github.pagehelper.PageInfo;
import com.ldz.park.dao.MarketMapper;
import com.ldz.park.model.Market;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.util.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketService {
    @Autowired
    private MarketMapper marketMapper;

    public ApiResponse list (){
        List<Market> list = marketMapper.list();
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



}
