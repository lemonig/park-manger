package com.ldz.park.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车位收藏实体（market_favorite）
 *
 * user_id + market_id 唯一（uk_user_market），接口层用
 * INSERT IGNORE / DELETE 保证收藏与取消幂等；
 * user/market 均为外键级联删除的物理关联（项目无逻辑删除惯例）。
 */
@Data
@Schema(description = "车位收藏")
public class MarketFavorite {

    private Long id;

    @Schema(description = "收藏人，user.id")
    private Integer userId;

    @Schema(description = "被收藏车位，market.id")
    private Long marketId;

    @Schema(description = "收藏时间")
    private LocalDateTime gmtCreate;
}
