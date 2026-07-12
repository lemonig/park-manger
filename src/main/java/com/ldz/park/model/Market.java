package com.ldz.park.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import com.ldz.park.model.vo.market.SimpleImage;
/**
 * 车位租售实体类（Market）
 *
 * 对应数据库表 market
 *
 * 设计说明：
 * 1. 使用 Lombok 注解简化代码（@Data 提供 getter/setter/toString/equals/hashCode，@Builder 支持链式构建，@NoArgsConstructor/@AllArgsConstructor 支持序列化/反序列化）。
 * 2. 字段类型选择：
 *    - id: Integer（表 int 自增，足够）。
 *    - price: BigDecimal（价格字段推荐精确类型，避免浮点误差）。
 *    - phone/parking_no/code 等 varchar: String。
 *    - status/type: Integer（int/tinylnt 常用 Integer）。
 *    - 时间字段: LocalDateTime（Java 8+ 标准，Spring Boot 3 完美支持序列化）。
 * 3. 字段名与数据库列名一致（驼峰转下划线 MyBatis 自动映射，或配置 map-underscore-to-camel-case=true）。
 * 4. 可为空字段用包装类型（Integer/String/LocalDateTime）支持 null。
 * 5. 纯 POJO（无 MyBatis-Plus 注解，兼容纯 MyBatis）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Market {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 业务唯一编码（关联图片中间表）
     */
    private String code;

    /**
     * 发布用户ID
     */
    private Integer userId;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 车位编号
     */
    private String parkingNo;

    /**
     * 详细描述
     */
    private String description;

    /**
     * 联系电话（用于点击拨打）
     */
    private String phone;

    /**
     * 位置描述
     */
    private String positionDesc;

    /**
     * 规格描述
     */
    private String specs;

    /**
     * 审核状态：0=待审核，1=通过，2=拒绝
     */
    private Integer status;

    /**
     * 审核通过时间（用于排序）
     */
    private LocalDateTime auditTime;

    /**
     * 城市（预留）
     */
    private String city;

    /**
     * 区域（预留）
     */
    private String area;

    /**
     * 小区（预留）
     */
    private String community;

    /**
     * 靠近楼栋（dict_item.value，逗号分隔）
     */
    private String buildings;

    /**
     * 类型：1=售卖，2=租赁
     */
    private Integer type;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;

    /**
     * 更新时间
     */
    private LocalDateTime gmtModify;

    /**
     * 关联图片列表（业务核心）
     * - 详情页：轮播展示所有图片
     * - 列表页：可取第一张作为缩略图，或显示图片数量
     * - Service 批量/单条填充
     */
    @Builder.Default
    private List<SimpleImage> images = new ArrayList<>();
}