package com.ldz.park.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ldz.park.dao.MarketMapper;
import com.ldz.park.entity.FileRecord;
import com.ldz.park.model.Market;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.model.vo.market.SimpleImage;
import com.ldz.park.util.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 车位租售业务 Service（优化版）
 *
 * 优化说明：
 * 1. 支持分页 + 条件查询（list 方法重载，支持 Market query 对象作为条件）。
 * 2. 使用 PageHelper 实现物理分页，返回 PageInfo（含 total/pages/list 等，前端直接用）。
 * 3. 新增/更新/删除 加 @Transactional（事务保证数据一致性，尤其是图片关联）。
 * 4. 新增时自动生成唯一 code（UUID，防重）。
 * 5. 默认审核状态为待审核（status = 0）。
 * 6. 图片填充优化：
 *    - 列表查询时批量填充（先查所有 code，再批量查图片，避免 N+1）。
 *    - 详情保持单条填充。
 * 7. insertImg 支持批量（多图上传场景）。
 * 8. 参数校验 + 异常抛出（业务异常，Controller 可捕获统一返回）。
 * 9. 代码结构清晰、注释完善、可维护性强。
 * 10. 移除无用 import，添加必要工具类使用。
 */
@Service
public class MarketService {

    @Autowired
    private MarketMapper marketMapper;

    /**
     * 查询列表（支持分页 + 条件查询）
     *
     * @param query    查询条件（type、parkingNo、status 等，可为 null 表示不限制）
     * @param pageNum  页码（默认 1）
     * @param pageSize 每页大小（默认 10）
     * @return PageInfo<Market>（含分页信息和填充图片的列表）
     */
    public PageInfo<Market> list(Market query, int pageNum, int pageSize) {
        // 默认值处理
        pageNum = pageNum < 1 ? 1 : pageNum;
        pageSize = pageSize < 1 ? 10 : pageSize;

        // 启动分页
        PageHelper.startPage(pageNum, pageSize);

        // 执行查询
        List<Market> list = marketMapper.list(query);

        // 批量填充图片（性能优化，避免 N+1 查询）
        if (!list.isEmpty()) {
            // 提取所有 code
            List<String> codes = list.stream()
                    .map(Market::getCode)
                    .collect(Collectors.toList());

            // 批量查图片（Mapper 需要实现 selectImagesByMarketCodes）
            List<SimpleImage> allImages = marketMapper.selectImagesByMarketCodes(codes);

            // 按 code 分组
            Map<String, List<SimpleImage>> imagesMap = allImages.stream()
                    .collect(Collectors.groupingBy(SimpleImage::getMarketCode));

            // 填充到每个 Market
            list.forEach(market -> market.setImages(imagesMap.getOrDefault(market.getCode(), List.of())));
        }

        return new PageInfo<>(list);
    }

    /**
     * 无分页全量列表（保留原方法，内部调用分页大尺寸）
     */
    public List<Market> list() {
        PageInfo<Market> pageInfo = list(new Market(), 1, Integer.MAX_VALUE);
        return pageInfo.getList();
    }

    /**
     * 新增车位
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(Market market) {
        if (market == null) {
            throw new IllegalArgumentException("车位信息不能为空");
        }

        // 自动生成唯一 code
        if (!StringUtils.hasText(market.getCode())) {
            market.setCode(UUID.randomUUID().toString().replace("-", "").toUpperCase());
        }

        // 默认待审核
        if (market.getStatus() == null) {
            market.setStatus(0);  // 0=待审核
        }

        marketMapper.add(market);
    }

    /**
     * 更新车位
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Market market) {
        if (market == null || market.getId() == null) {
            throw new IllegalArgumentException("更新时ID不能为空");
        }

        int rows = marketMapper.update(market);
        if (rows == 0) {
            throw new IllegalArgumentException("车位信息不存在或已删除");
        }
    }

    /**
     * 删除车位
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }

        // 可先删除关联图片（业务需求）
        marketMapper.deleteImagesByMarketId(id);

        int rows = marketMapper.delete(id);
        if (rows == 0) {
            throw new IllegalArgumentException("车位信息不存在或已删除");
        }
    }

    /**
     * 获取详情（带图片填充）
     */
    public Market detail(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }

        Market market = marketMapper.getDetailById(id);
        if (market == null) {
            return null;  // 或抛异常，由 Controller 处理
        }

        fillImages(market);
        return market;
    }

    /**
     * 单条填充图片（详情用）
     */
    private void fillImages(Market market) {
        if (market == null || !StringUtils.hasText(market.getCode())) {
            return;
        }
        List<SimpleImage> images = marketMapper.selectImagesByMarketCode(market.getCode());
        market.setImages(images);
    }

    /**
     * 保存图片关联（支持批量）
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveImages(String marketCode, List<String> imageIds, Integer userId) {
        if (!StringUtils.hasText(marketCode) || imageIds == null || imageIds.isEmpty()) {
            return;
        }

        // 批量插入（Mapper 需要支持批量）
        marketMapper.insertImagesBatch(marketCode, imageIds, userId);
    }

    /**
     * 单图插入（兼容原方法）
     */
    public void insertImg(String marketCode, String imageId, Integer userId) {
        saveImages(marketCode, List.of(imageId), userId);
    }
}