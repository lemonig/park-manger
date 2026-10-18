package com.ldz.park.dao;

import com.ldz.park.model.Feedback;
import com.ldz.park.model.vo.market.SimpleImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FeedbackMapper {

    List<Feedback> list(Feedback query);

    void add(Feedback feedback);

    Feedback getDetailById(Long id);

    /**
     * 标记已处理：仅 status=0（待处理）时可处理，返回影响行数
     */
    int handle(Feedback feedback);

    /**
     * 统计用户最近 N 小时内提交的反馈数（频控用）
     */
    int countRecentByUser(@Param("userId") Integer userId, @Param("hours") Integer hours);

    /**
     * 校验图片 id 是否都存在于 images 表（防外链注入）
     */
    int countExistingImages(@Param("imageIds") List<String> imageIds);

    List<SimpleImage> selectImagesByFeedbackCodes(@Param("codes") List<String> codes);

    List<SimpleImage> selectImagesByFeedbackCode(@Param("feedbackCode") String feedbackCode);

    int insertImagesBatch(@Param("feedbackCode") String feedbackCode,
                          @Param("imageIds") List<String> imageIds,
                          @Param("userId") Integer userId);
}
