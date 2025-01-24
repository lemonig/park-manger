package com.ldz.park.web;

import com.ldz.park.model.Carport;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.CarportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "车位管理", description = "车位相关接口")
@RestController
@RequestMapping("/api/carport")
public class CarportController {
    @Autowired
    private CarportService carportService;

    @Operation(summary = "获取车位列表")
    @PostMapping("/list")
    public ApiResponse carportList(HttpServletRequest request) {
        return carportService.list();
    }

    @Operation(summary = "添加车位")
    @PostMapping("/add")
    public ApiResponse carportAdd(HttpServletRequest request, @RequestBody Carport carport) {
        carportService.add(carport);
        return new ApiResponse();
    }

    @Operation(summary = "更新车位信息")
    @PostMapping("/update")
    public ApiResponse carportUpdate(HttpServletRequest request, @RequestBody Carport carport) {
        carportService.update(carport);
        return new ApiResponse();
    }

    @Operation(summary = "删除车位")
    @PostMapping(value = "/delete")
    public ApiResponse delete(@RequestBody Carport carport) {
        ApiResponse apiResponse = new ApiResponse();
        carportService.delete(carport.getId());
        return apiResponse;
    }

    @Operation(summary = "获取车位详情")
    @PostMapping(value = "/detail")
    public ApiResponse detail(@RequestBody Carport carport) {
        return carportService.detail(carport.getId());
    }
}
