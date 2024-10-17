package com.ldz.park.service;

import com.ldz.park.dao.MarketMapper;
import com.ldz.park.model.Market;
import com.ldz.park.model.meta.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketService {
    @Autowired
    private MarketMapper marketMapper;

    public ApiResponse list (){
        List<Market> list = marketMapper.list();
        System.out.println(list);
        return new ApiResponse(list);
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
