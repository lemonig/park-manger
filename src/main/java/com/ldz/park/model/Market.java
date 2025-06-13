package com.ldz.park.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class Market {
    private Integer id;
    private String code;
    private Integer ownerId;
    private BigDecimal price;
    private Integer number;
    private String description;
    private List<String> photo;
//    private String photos;
    private Integer type;

}
