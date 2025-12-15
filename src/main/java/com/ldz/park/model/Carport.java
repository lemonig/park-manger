package com.ldz.park.model;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "车位信息")
@Data
public class Carport {

    @Schema(description = "车位ID")
    private Integer id;

    @Schema(description = "车位编号")
    private Integer number;

    private String description;
private List<String> photo;
}
