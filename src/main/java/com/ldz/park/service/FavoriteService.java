package com.ldz.park.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ldz.park.dao.FavoriteMapper;
import com.ldz.park.dao.MarketMapper;
import com.ldz.park.model.Market;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 车位收藏 Service
 *
 * 幂等设计：收藏走 INSERT IGNORE（uk_user_market 冲突即忽略）、
 * 取消走物理 DELETE（删 0 行同样视为成功），客户端可放心重试；
 * 收藏列表与首页口径一致：只返回在售（status=1）车位，
 * 已下架/未过审的车位自动从收藏列表消失。
 */
@Service
public class FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private MarketMapper marketMapper;

    @Autowired
    private MarketService marketService;

    /**
     * 收藏车位（幂等，重复收藏不报错）
     */
    public void add(Integer userId, Long marketId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户身份验证失败");
        }
        if (marketId == null) {
            throw new IllegalArgumentException("车位ID不能为空");
        }
        if (marketMapper.getDetailById(marketId) == null) {
            throw new IllegalArgumentException("车位信息不存在");
        }
        favoriteMapper.insertIgnore(userId, marketId);
    }

    /**
     * 取消收藏（幂等，未收藏也视为成功）
     */
    public void remove(Integer userId, Long marketId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户身份验证失败");
        }
        if (marketId == null) {
            throw new IllegalArgumentException("车位ID不能为空");
        }
        favoriteMapper.delete(userId, marketId);
    }

    /**
     * 我的收藏列表（分页，含图片，仅在售）
     */
    public PageInfo<Market> list(Integer userId, int pageNum, int pageSize) {
        if (userId == null) {
            throw new IllegalArgumentException("用户身份验证失败");
        }
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.max(pageSize, 1);

        PageHelper.startPage(pageNum, pageSize);
        List<Market> list = favoriteMapper.listMarketsByUser(userId);
        marketService.batchFillImages(list);
        return new PageInfo<>(list);
    }

    /**
     * 收藏的 marketId 集合（首页星标状态回显）
     */
    public List<Long> listIds(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户身份验证失败");
        }
        return favoriteMapper.listIds(userId);
    }
}
