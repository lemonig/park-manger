package com.ldz.park.dao;

import com.ldz.park.model.Community;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommunityMapper {

    /**
     * 条件查询（keyword 模糊、city/area/status 精确），按 id 升序保证稳定顺序；
     * 「附近排序」由 Service 层按 lat/lng 在内存中计算。
     */
    List<Community> list(@Param("keyword") String keyword,
                         @Param("city") String city,
                         @Param("area") String area,
                         @Param("status") Integer status);

    Community detail(Long id);

    /**
     * 同名同城市重复检查用
     */
    Community getByNameAndCity(@Param("name") String name, @Param("city") String city);

    void add(Community community);

    int update(Community community);

    int delete(Long id);

    /**
     * 引用计数：删除小区前检查是否已被 market 挂接
     */
    int countMarketByCommunityId(@Param("communityId") Long communityId);
}
