package com.ldz.park.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ldz.park.dao.FeedbackMapper;
import com.ldz.park.model.DictItem;
import com.ldz.park.model.Feedback;
import com.ldz.park.model.vo.market.SimpleImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 意见反馈业务 Service
 *
 * 业务说明：
 * 1. 提交（小程序）：userId 一律取自 JWT，类型走字典 feedback_type 校验，
 *    内容 10-500 字，图片 ≤ 4 张且必须是本系统 images 表已有的 id，
 *    24 小时内同一用户最多 5 条。
 * 2. 处理（后台）：仅待处理状态可标记已处理，处理备注必填，幂等由影响行数判断。
 * 3. 列表/详情自动填充图片，模式与 MarketService 一致。
 */
@Service
public class FeedbackService {

    private static final String FEEDBACK_TYPE_DICT = "feedback_type";
    private static final int MAX_IMAGE_COUNT = 4;
    private static final int RATE_LIMIT_HOURS = 24;
    private static final int RATE_LIMIT_COUNT = 5;
    private static final int CONTENT_MIN_LENGTH = 10;
    private static final int CONTENT_MAX_LENGTH = 500;
    private static final int CONTACT_MAX_LENGTH = 20;
    private static final int REMARK_MAX_LENGTH = 200;

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private DictService dictService;

    /**
     * 提交反馈（小程序）
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(Feedback feedback, Integer userId) {
        if (feedback == null || userId == null) {
            throw new IllegalArgumentException("用户身份验证失败");
        }
        validateType(feedback.getType());

        String content = feedback.getContent() == null ? "" : feedback.getContent().trim();
        if (content.length() < CONTENT_MIN_LENGTH) {
            throw new IllegalArgumentException("反馈内容至少" + CONTENT_MIN_LENGTH + "个字");
        }
        if (content.length() > CONTENT_MAX_LENGTH) {
            throw new IllegalArgumentException("反馈内容不能超过" + CONTENT_MAX_LENGTH + "个字");
        }

        String contact = feedback.getContact() == null ? "" : feedback.getContact().trim();
        if (contact.length() > CONTACT_MAX_LENGTH) {
            throw new IllegalArgumentException("联系方式不能超过" + CONTACT_MAX_LENGTH + "个字符");
        }

        List<String> imageIds = feedback.getImages() == null ? new ArrayList<>() : feedback.getImages().stream()
                .map(SimpleImage::getId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (imageIds.size() > MAX_IMAGE_COUNT) {
            throw new IllegalArgumentException("最多上传" + MAX_IMAGE_COUNT + "张截图");
        }
        if (!imageIds.isEmpty() && feedbackMapper.countExistingImages(imageIds) != imageIds.size()) {
            throw new IllegalArgumentException("截图不存在或已失效，请重新上传");
        }

        if (feedbackMapper.countRecentByUser(userId, RATE_LIMIT_HOURS) >= RATE_LIMIT_COUNT) {
            throw new IllegalArgumentException("反馈提交太频繁，请明天再来");
        }

        Feedback record = new Feedback();
        record.setCode(UUID.randomUUID().toString().replace("-", "").toUpperCase());
        record.setUserId(userId);
        record.setType(feedback.getType());
        record.setContent(content);
        record.setContact(StringUtils.hasText(contact) ? contact : null);
        record.setStatus(0);
        feedbackMapper.add(record);

        if (!imageIds.isEmpty()) {
            feedbackMapper.insertImagesBatch(record.getCode(), imageIds, userId);
        }
    }

    /**
     * 后台列表（分页 + 条件，已填充图片与提交人信息）
     */
    public PageInfo<Feedback> list(Feedback query, int pageNum, int pageSize) {
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.max(pageSize, 1);
        PageHelper.startPage(pageNum, pageSize);
        List<Feedback> list = feedbackMapper.list(query == null ? new Feedback() : query);
        batchFillImages(list);
        return new PageInfo<>(list);
    }

    /**
     * 详情（已填充图片）
     */
    public Feedback detail(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        Feedback feedback = feedbackMapper.getDetailById(id);
        if (feedback != null) {
            fillImages(feedback);
        }
        return feedback;
    }

    /**
     * 标记已处理（后台）。仅待处理可处理，重复处理报错。
     */
    @Transactional(rollbackFor = Exception.class)
    public void handle(Long id, String handleRemark, Integer adminId) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        if (adminId == null) {
            throw new IllegalArgumentException("用户身份验证失败");
        }
        if (!StringUtils.hasText(handleRemark)) {
            throw new IllegalArgumentException("处理备注不能为空");
        }
        String remark = handleRemark.trim();
        if (remark.length() > REMARK_MAX_LENGTH) {
            throw new IllegalArgumentException("处理备注不能超过" + REMARK_MAX_LENGTH + "个字");
        }
        Feedback existing = feedbackMapper.getDetailById(id);
        if (existing == null) {
            throw new IllegalArgumentException("反馈不存在");
        }

        Feedback patch = new Feedback();
        patch.setId(id);
        patch.setStatus(1);
        patch.setHandleRemark(remark);
        patch.setHandleBy(adminId);
        patch.setHandleTime(LocalDateTime.now());
        if (feedbackMapper.handle(patch) == 0) {
            throw new IllegalArgumentException("该反馈已处理");
        }
    }

    private void validateType(String type) {
        if (!StringUtils.hasText(type)) {
            throw new IllegalArgumentException("请选择反馈类型");
        }
        List<DictItem> items = dictService.listItemsByTypeCode(FEEDBACK_TYPE_DICT);
        boolean valid = items != null && items.stream().anyMatch(item ->
                type.equals(item.getValue()) && (item.getStatus() == null || item.getStatus() == 1));
        if (!valid) {
            throw new IllegalArgumentException("反馈类型无效，请刷新后重选");
        }
    }

    /**
     * 批量填充图片（列表查询用，1+1 查询 + groupingBy 分组）
     */
    private void batchFillImages(List<Feedback> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<String> codes = list.stream()
                .map(Feedback::getCode)
                .distinct()
                .collect(Collectors.toList());
        List<SimpleImage> allImages = feedbackMapper.selectImagesByFeedbackCodes(codes);
        Map<String, List<SimpleImage>> imagesMap = allImages.stream()
                .collect(Collectors.groupingBy(SimpleImage::getMarketCode));
        list.forEach(feedback -> feedback.setImages(
                imagesMap.getOrDefault(feedback.getCode(), new ArrayList<>())));
    }

    /**
     * 单条填充图片（详情查询用）
     */
    private void fillImages(Feedback feedback) {
        if (feedback == null) {
            return;
        }
        if (!StringUtils.hasText(feedback.getCode())) {
            feedback.setImages(new ArrayList<>());
            return;
        }
        List<SimpleImage> images = feedbackMapper.selectImagesByFeedbackCode(feedback.getCode());
        feedback.setImages(images == null ? new ArrayList<>() : images);
    }
}
