package com.ldz.park.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 小区字典实体（community）
 *
 * 车位租售标的地址的唯一来源：market.city/area/community 由服务端按
 * 选中小区回写，不再取自填报人 GPS。lat/lng 用于小程序「附近小区」
 * 排序与后续地图聚合。
 */
@Data
@Schema(description = "小区字典")
public class Community {

    private Long id;

    @Schema(description = "规范小区名")
    private String name;

    @Schema(description = "省")
    private String province;

    @Schema(description = "市")
    private String city;

    @Schema(description = "区/县")
    private String area;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "纬度（gcj02）")
    private BigDecimal lat;

    @Schema(description = "经度（gcj02）")
    private BigDecimal lng;

    @Schema(description = "状态：1=启用，0=停用")
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 非库字段：与定位点距离（米），仅「附近小区」排序接口返回
     */
    @Schema(description = "与定位点距离（米），仅附近排序时返回")
    private transient Double distanceMeters;
}
