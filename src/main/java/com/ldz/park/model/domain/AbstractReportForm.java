package com.ldz.park.model.domain;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class AbstractReportForm {

    private Integer id;

    private Integer user_id;

    /**
     * 描述
     */
    private String description;

    /**
     * 照片
     */
    private List<String> pictures;


    private String fee_type_str;

    private Timestamp gmt_create;
}