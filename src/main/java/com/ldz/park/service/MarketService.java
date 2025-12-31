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

    /**
     * 查询市场列表（不关心分页表现）
     */
    public List<Market> list() {
        List<Market> list = marketMapper.list();
        list.forEach(this::fillImages);
        return list;
    }

    /**
     * 新增市场
     */
    public void add(Market market) {
        marketMapper.add(market);
    }

    /**
     * 更新市场
     */
    public void update(Market market) {
        marketMapper.update(market);
    }

    /**
     * 删除
     */
    public void delete(Integer id) {
        marketMapper.delete(id);
    }

    /**
     * 获取详情
     */
    public Market detail(Integer id) {
        Market market = marketMapper.getDetailById(id);
        fillImages(market);
        return market;
    }

    /**
     * 关联图片
     */
    private void fillImages(Market market) {
        if (market == null) return;
        List<SimpleImage> images =
                marketMapper.selectImagesByMarketCode(market.getCode());
        market.setImages(images);
    }

    /**
     * 保存图片关联
     */
    public void insertImg(String marketCode, String imageId, Integer userId) {
        marketMapper.insertImg(marketCode, imageId, userId);
    }
}
