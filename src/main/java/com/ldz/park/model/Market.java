package com.ldz.park.model;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Market {
    private Integer id;
    private String code;
    private Integer ownerId;
    private BigDecimal price;
    private String number;
    private String description;

    // 防止 photo 为空的写法
    private List<Map<String, String>> photo = new ArrayList<>();

    //    private String photos;
    private String imageId;
    private Integer type;

    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModify;
}