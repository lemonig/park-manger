package com.ldz.park.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ldz.park.dao.CommunityMapper;
import com.ldz.park.model.Community;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

/**
 * 小区字典业务 Service。
 *
 * 车位租售的标的地址以小区库为唯一来源（详见 docs/sql/community-init.sql）：
 * - 小程序端 listForPicker：只出启用小区，带定位时按距离排序（GPS 仅是排序信号，不是地址来源）。
 * - 管理端：CRUD + 删除前引用检查。
 * - requireEnabled：供 MarketService 发布/编辑车位时解析小区并回写冗余地址字段。
 */
@Service
@RequiredArgsConstructor
public class CommunityService {

    /**
     * 选择器单次拉取上限：小区库为运营维护的受控字典，
     * 单城市规模有限，内存排序 + 截断即可，无需 SQL 距离排序。
     */
    private static final int PICKER_LIMIT = 200;

    private final CommunityMapper communityMapper;

    /**
     * 小程序选择器：keyword/city/area 过滤 + 可选定位附近排序。
     */
    public List<Community> listForPicker(String keyword, String city, String area, Double lat, Double lng) {
        PageHelper.startPage(1, PICKER_LIMIT, false);
        List<Community> list = communityMapper.list(keyword, city, area, 1);
        if (lat != null && lng != null && !list.isEmpty()) {
            list.forEach(c -> c.setDistanceMeters(distanceMeters(lat, lng, c)));
            list.sort(Comparator.comparing(
                    Community::getDistanceMeters,
                    Comparator.nullsLast(Comparator.naturalOrder())));
        }
        return list;
    }

    /**
     * 管理端分页列表（含停用）。
     */
    public PageInfo<Community> listAdmin(String keyword, String city, String area, Integer status,
                                         int pageNum, int pageSize) {
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.max(pageSize, 1);
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(communityMapper.list(keyword, city, area, status));
    }

    public Community detail(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        return communityMapper.detail(id);
    }

    public void add(Community community) {
        validate(community);
        Community exists = communityMapper.getByNameAndCity(community.getName(), community.getCity());
        if (exists != null) {
            throw new IllegalArgumentException("该城市下已存在同名小区，请勿重复添加");
        }
        communityMapper.add(community);
    }

    public void update(Community community) {
        if (community == null || community.getId() == null) {
            throw new IllegalArgumentException("更新时ID不能为空");
        }
        validate(community);
        Community exists = communityMapper.getByNameAndCity(community.getName(), community.getCity());
        if (exists != null && !exists.getId().equals(community.getId())) {
            throw new IllegalArgumentException("该城市下已存在同名小区，请勿重复添加");
        }
        if (communityMapper.update(community) == 0) {
            throw new IllegalArgumentException("小区不存在或已删除");
        }
    }

    /**
     * 删除前检查引用：已被车位挂接的小区不允许删除（避免 market.community_id 悬空），
     * 可改为停用（status=0）。
     */
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        if (communityMapper.countMarketByCommunityId(id) > 0) {
            throw new IllegalArgumentException("该小区已被车位信息引用，不能删除，可改为停用");
        }
        if (communityMapper.delete(id) == 0) {
            throw new IllegalArgumentException("小区不存在或已删除");
        }
    }

    /**
     * 供 MarketService 解析选中小区：必须存在且启用。
     * 解析结果回写 market 的冗余地址字段，保证标的地址与小区库一致。
     */
    public Community requireEnabled(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("请选择车位所在小区");
        }
        Community community = communityMapper.detail(id);
        if (community == null || community.getStatus() == null || community.getStatus() != 1) {
            throw new IllegalArgumentException("所选小区不存在或已停用，请重新选择");
        }
        return community;
    }

    private void validate(Community community) {
        if (community == null || !StringUtils.hasText(community.getName())) {
            throw new IllegalArgumentException("小区名称不能为空");
        }
        if (community.getName().length() > 100) {
            throw new IllegalArgumentException("小区名称不能超过100个字符");
        }
        if (!StringUtils.hasText(community.getCity())) {
            throw new IllegalArgumentException("城市不能为空");
        }
    }

    /**
     * 球面距离（米），Haversine 公式；小区缺坐标时返回 null 不参与排序。
     */
    private Double distanceMeters(double lat, double lng, Community community) {
        if (community.getLat() == null || community.getLng() == null) {
            return null;
        }
        double earthRadius = 6371000;
        double radLat1 = Math.toRadians(lat);
        double radLat2 = Math.toRadians(community.getLat().doubleValue());
        double deltaLat = Math.toRadians(community.getLat().doubleValue() - lat);
        double deltaLng = Math.toRadians(community.getLng().doubleValue() - lng);
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        return earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
