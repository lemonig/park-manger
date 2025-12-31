package com.ldz.park.web;

import com.ldz.park.model.Carport;
import com.ldz.park.model.Market;
import com.ldz.park.model.meta.ApiErrorResponse;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.model.vo.market.SimpleImage;
import com.ldz.park.service.MarketService;
import com.ldz.park.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    @Autowired
    private MarketService marketService;

    @PostMapping("/list")
    public ApiResponse<List<Market>> list() {
        List<Market> list = marketService.list();
        return ApiResponse.success(list);
    }

    @PostMapping("/detail")
    public ApiResponse<Market> detail(@RequestBody Market market) {
        return ApiResponse.success(
                marketService.detail(market.getId())
        );
    }

    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestBody Market market) {
        marketService.add(market);
        return ApiResponse.success("添加成功", null);
    }

    @PostMapping("/update")
    public ApiResponse<Void> update(@RequestBody Market market) {
        marketService.update(market);
        return ApiResponse.success("更新成功", null);
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody Market market) {
        marketService.delete(market.getId());
        return ApiResponse.success("删除成功", null);
    }
}
