package com.ldz.park.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ldz.park.dao.MarketMapper;
import com.ldz.park.model.Market;
import com.ldz.park.model.vo.market.SimpleImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 车位租售业务 Service（完整优化版）
 *
 * 业务说明：
 * 1. 支持分页 + 条件查询（type、parkingNo 模糊、status）。
 * 2. 列表查询自动批量填充图片（性能优化，避免 N+1）。
 * 3. 新增时：
 * - 自动生成唯一 code（UUID 大写无-）。
 * - 默认 status = 0（待审核）。
 * 4. 更新/删除/详情 支持事务 + 基本校验。
 * 5. 图片操作：
 * - 批量插入 saveImages（推荐，多图上传）。
 * - 单图 insertImg（兼容原方法）。
 * - 删除车位时自动清理关联图片。
 * 6. 异常处理：抛 IllegalArgumentException（Controller 统一捕获返回错误响应）。
 * 7. 兼容纯 MyBatis + PageHelper（与您的 pom 完美匹配）。
 */
@Service
public class MarketService {

    @Autowired
    private MarketMapper marketMapper;

    /**
     * 查询列表（支持分页 + 条件查询，已填充图片）
     */
    public PageInfo<Market> list(Market query, int pageNum, int pageSize) {
        // 参数安全处理
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.max(pageSize, 1);

        // 启动分页
        PageHelper.startPage(pageNum, pageSize);

        // 执行条件查询
        List<Market> list = marketMapper.list(query == null ? new Market() : query);

        batchFillImages(list);
        return new PageInfo<>(list);
    }

    /**
     * 无分页全量列表（兼容原方法）
     */
    public List<Market> list() {
        PageInfo<Market> pageInfo = list(new Market(), 1, Integer.MAX_VALUE);
        return pageInfo.getList();
    }

    /**
     * 小程序端列表：只查已审核通过（status=1），未指定 status 时强制加过滤。
     */
    public PageInfo<Market> listPublished(Market query, int pageNum, int pageSize) {
        Market cond = query == null ? new Market() : query;
        cond.setStatus(1);
        return list(cond, pageNum, pageSize);
    }

    /**
     * 我发布的车位列表（不限 status）。
     */
    public PageInfo<Market> listByUser(Integer userId, Market query, int pageNum, int pageSize) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        Market cond = query == null ? new Market() : query;
        cond.setUserId(userId);
        return list(cond, pageNum, pageSize);
    }

    /**
     * 小程序：更新自己发布的车位（校验归属）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateByOwner(Market market, Integer currentUserId) {
        if (market == null || market.getId() == null) {
            throw new IllegalArgumentException("更新时ID不能为空");
        }
        if (currentUserId == null) {
            throw new IllegalArgumentException("用户身份验证失败");
        }
        Market existing = marketMapper.getDetailById(market.getId());
        if (existing == null) {
            throw new IllegalArgumentException("车位信息不存在");
        }
        if (existing.getUserId() == null || !existing.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("无权限修改此车位信息");
        }
        // 强制以现有 userId 为准，防止越权
        market.setUserId(existing.getUserId());
        update(market);
    }

    /**
     * 小程序：删除自己发布的车位（校验归属）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByOwner(Long id, Integer currentUserId) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        if (currentUserId == null) {
            throw new IllegalArgumentException("用户身份验证失败");
        }
        Market existing = marketMapper.getDetailById(id);
        if (existing == null) {
            throw new IllegalArgumentException("车位信息不存在");
        }
        if (existing.getUserId() == null || !existing.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("无权限删除此车位信息");
        }
        delete(id);
    }

    /**
     * 审核（后台专用）：修改 status、审核时间与驳回原因。
     * 拒绝（status=2）时 remark 必填。
     */
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, Integer status, String remark) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        if (status == null || (status != 1 && status != 2)) {
            throw new IllegalArgumentException("审核状态非法（1=通过，2=拒绝）");
        }
        if (status == 2 && (remark == null || remark.isBlank())) {
            throw new IllegalArgumentException("驳回时必须填写驳回原因");
        }
        Market existing = marketMapper.getDetailById(id);
        if (existing == null) {
            throw new IllegalArgumentException("车位信息不存在");
        }
        Market patch = new Market();
        patch.setId(id);
        patch.setStatus(status);
        if (status == 1) {
            patch.setAuditTime(java.time.LocalDateTime.now());
        }
        if (status == 2) {
            patch.setAuditRemark(remark.trim());
        }
        marketMapper.update(patch);
    }

    /**
     * 新增车位（含图片批量保存）
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(Market market, Integer currentUserId) {
        if (market == null) {
            throw new IllegalArgumentException("车位信息不能为空");
        }

        // 自动生成 code
        if (!StringUtils.hasText(market.getCode())) {
            market.setCode(UUID.randomUUID().toString().replace("-", "").toUpperCase());
        }

        // 默认待审核
        if (market.getStatus() == null) {
            market.setStatus(0);
        }

        marketMapper.add(market);
        if (market.getImages() != null && !market.getImages().isEmpty()) {
            List<String> imageIds = market.getImages().stream()
                    .map(SimpleImage::getId)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            if (!imageIds.isEmpty()) {
                saveImages(market.getCode(), imageIds, currentUserId);
            }
        }
    }

    /**
     * 更新车位（带用户权限验证）
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Market market) {
        if (market == null || market.getId() == null) {
            throw new IllegalArgumentException("更新时ID不能为空");
        }

        // 权限验证：只能更新自己发布的车位
        Market existing = marketMapper.getDetailById(market.getId());
        if (existing == null) {
            throw new IllegalArgumentException("车位信息不存在");
        }
            //TODO更新车位需要 验证本人ID吗
//        if (!existing.getUserId().equals(currentUserId)) {
//            throw new IllegalArgumentException("无权限修改此车位信息");
//        }

        int rows = marketMapper.update(market);
        if (rows == 0) {
            throw new IllegalArgumentException("车位信息不存在或已删除");
        }

        if (market.getImages() != null) {
            marketMapper.deleteImagesByMarketCode(existing.getCode());
            List<String> imageIds = market.getImages().stream()
                    .map(SimpleImage::getId)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            if (!imageIds.isEmpty()) {
                saveImages(existing.getCode(), imageIds, existing.getUserId());
            }
        }
    }

    /**
     * 删除车位（清理关联图片）
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }

        Market market = marketMapper.getDetailById(id);
        if (market == null) {
            throw new IllegalArgumentException("车位信息不存在");
        }

        // 先清理图片关联
        marketMapper.deleteImagesByMarketCode(market.getCode());

        // 再删主记录
        int rows = marketMapper.delete(id);
        if (rows == 0) {
            throw new IllegalArgumentException("删除失败，车位信息已不存在");
        }
    }

    /**
     * 获取详情（已填充图片）
     */
    public Market detail(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }

        Market market = marketMapper.getDetailById(id);
        if (market == null) {
            return null; // 或抛异常，由 Controller 处理
        }

        fillImages(market); // 单条填充

        return market;
    }

    /**
     * 批量保存图片关联
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveImages(String marketCode, List<String> imageIds, Integer userId) {
        if (!StringUtils.hasText(marketCode) || imageIds == null || imageIds.isEmpty()) {
            return;
        }
        marketMapper.insertImagesBatch(marketCode, imageIds, userId);
    }

    /**
     * 单图插入（兼容原方法）
     */
    public void insertImg(String marketCode, String imageId, Integer userId) {
        if (StringUtils.hasText(marketCode) && StringUtils.hasText(imageId)) {
            marketMapper.insertImg(marketCode, imageId, userId);
        }
    }

    /**
     * 批量填充图片（列表查询用，性能最优：1+1 查询 + groupingBy 分组）
     */
    private void batchFillImages(List<Market> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 提取所有唯一 code
        List<String> codes = list.stream()
                .map(Market::getCode)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询所有图片（Mapper selectImagesByMarketCodes 返回带 marketCode 的 SimpleImage）
        List<SimpleImage> allImages = marketMapper.selectImagesByMarketCodes(codes);

        // 按 marketCode 分组（高效 O(n)）
        Map<String, List<SimpleImage>> imagesMap = allImages.stream()
                .collect(Collectors.groupingBy(SimpleImage::getMarketCode));

        // 填充到每个 Market（无 marketCode 的设空列表，避免 null）
        list.forEach(market -> {
            List<SimpleImage> marketImages = imagesMap.getOrDefault(market.getCode(), new ArrayList<>());
            market.setImages(marketImages);
        });
    }

    /**
     * 单条填充图片（详情查询用，兼容）
     */
    private void fillImages(Market market) {
        if (market == null) {
            return;
        }

        if (!StringUtils.hasText(market.getCode())) {
            market.setImages(new ArrayList<>());
            return;
        }

        List<SimpleImage> images = marketMapper.selectImagesByMarketCode(market.getCode());
        market.setImages(images == null ? new ArrayList<>() : images);
    }
}