package com.ldz.park.model.vo.market;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class SimpleImage {
    private String id;
    private String url;
    /**
     * 关联市场编码（后端分组用，前端可忽略）
     * 如果不想序列化到前端，加 @JsonIgnore
     */
    @JsonIgnore
    private String marketCode;
}
