package com.ldz.park.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 市场与图片关联表实体类（MarketImages）
 *
 * 对应数据库表 market_images（中间表）
 *
 * 设计说明：
 * 1. 使用 Lombok 注解简化代码（@Data 提供 getter/setter/toString/equals/hashCode，@Builder 支持链式构建）。
 * 2. 字段类型选择：
 *    - id: Integer（int 自增）。
 *    - market_code/image_id: String（varchar(100)）。
 *    - create_by: Integer（int 可空）。
 *    - create_time: LocalDateTime（datetime 默认当前时间）。
 * 3. 字段名驼峰映射（MyBatis 配置 map-underscore-to-camel-case=true 自动处理）。
 * 4. 作为中间表实体，主要用于批量插入/查询关联图片（Service 批量 saveImages 时可用）。
 * 5. 不需额外业务字段（纯关联表）。
 * 6. 如果项目有 BaseEntity（id + createTime 等），可继承；当前独立设计。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketImages {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 关联市场编码（market.code）
     */
    private String marketCode;

    /**
     * 关联图片ID（files.id 或 COS key）
     */
    private String imageId;

    /**
     * 创建人ID（用户ID）
     */
    private Integer createBy;

    /**
     * 创建时间（默认 CURRENT_TIMESTAMP）
     */
    private LocalDateTime createTime;
}