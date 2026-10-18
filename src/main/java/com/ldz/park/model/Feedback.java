package com.ldz.park.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ldz.park.model.vo.market.SimpleImage;

/**
 * 意见反馈实体类（Feedback）
 *
 * 对应数据库表 feedback
 *
 * 说明：
 * 1. 反馈类型 type 为字典 feedback_type 的 value（bug/advice/other），Service 校验有效性。
 * 2. status：0=待处理，1=已处理。
 * 3. 图片关联表 feedback_images，模式与 market_images 一致，列表/详情由 Service 填充。
 * 4. userName/userDoorplate/userMobile 为后台列表联查 user 表的展示字段，非本表列。
 * 5. keyword/startTime/endTime 仅作为后台列表查询条件使用，非本表列。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 业务唯一编码（关联图片中间表）
     */
    private String code;

    /**
     * 提交人 user.id（后端从 JWT 解出，不信任前端传入）
     */
    private Integer userId;

    /**
     * 反馈类型（字典 feedback_type 的 value）
     */
    private String type;

    /**
     * 反馈内容（10-500 字）
     */
    private String content;

    /**
     * 联系方式（用户手填，选填）
     */
    private String contact;

    /**
     * 处理状态：0=待处理，1=已处理
     */
    private Integer status;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 处理人 user.id
     */
    private Integer handleBy;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;

    /**
     * 更新时间
     */
    private LocalDateTime gmtModify;

    /**
     * 提交人姓名（联查展示字段）
     */
    private String userName;

    /**
     * 提交人门牌（联查展示字段）
     */
    private String userDoorplate;

    /**
     * 提交人手机号（联查展示字段）
     */
    private String userMobile;

    /**
     * 关联图片列表（Service 填充）
     */
    @Builder.Default
    private List<SimpleImage> images = new ArrayList<>();

    /**
     * 关键字（后台查询条件：内容/联系方式/提交人）
     */
    private String keyword;

    /**
     * 提交时间范围-起（后台查询条件）
     */
    private String startTime;

    /**
     * 提交时间范围-止（后台查询条件）
     */
    private String endTime;
}
