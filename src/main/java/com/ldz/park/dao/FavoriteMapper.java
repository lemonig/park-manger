package com.ldz.park.dao;

import com.ldz.park.model.Market;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    /**
     * 收藏（uk_user_market 冲突时忽略，天然幂等）
     *
     * @return 实际插入行数：0 = 已收藏过
     */
    int insertIgnore(@Param("userId") Integer userId, @Param("marketId") Long marketId);

    /**
     * 取消收藏（物理删除）
     *
     * @return 删除行数：0 = 原本未收藏
     */
    int delete(@Param("userId") Integer userId, @Param("marketId") Long marketId);

    /**
     * 收藏的车位列表（join market，只含在售 status=1，与首页口径一致），
     * 按收藏时间倒序；分页由 PageHelper 在 Service 层套用
     */
    List<Market> listMarketsByUser(@Param("userId") Integer userId);

    /**
     * 收藏的 marketId 集合（首页星标状态回显用），按收藏时间倒序
     */
    List<Long> listIds(@Param("userId") Integer userId);
}
