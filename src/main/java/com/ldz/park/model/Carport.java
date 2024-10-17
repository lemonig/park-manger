package com.ldz.park.model;

import lombok.Data;

import java.util.List;
@Data
public class Carport {

    private Integer id;
    private Integer number;
    private String description;
    private List<String> photo;
}
