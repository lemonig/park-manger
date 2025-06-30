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
    MarketService marketService;

    @Autowired
    UserService userService;

    @PostMapping(value = "/list")
    public ApiResponse marketList(HttpServletRequest request){
        return marketService.list();
    }

    @PostMapping(value = "/add")
    public ApiResponse marketAdd(HttpServletRequest request, @RequestBody Market market) {
        // 生成唯一编码
        String code = UUID.randomUUID().toString().replace("-", "");
        market.setCode(code);

        // 获取请求头中的 token
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
            apiErrorResponse.setMessage("token<UNK>");
            return apiErrorResponse;
        }

        // 通过 token 获取用户 ID
        Integer userId = userService.getUserIdByToken(token);
        if (userId == null) {
            ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
            apiErrorResponse.setMessage("Invalid token");
            return apiErrorResponse;
        }
        market.setOwnerId(userId);

        // 设置创建时间和修改时间
        market.setGmtCreate(LocalDateTime.now());
        market.setGmtModify(LocalDateTime.now());

        // 获取图片列表，校验是否为空
        List<SimpleImage> images = market.getImages();
        if (images != null && !images.isEmpty()) {
            for (SimpleImage image : images) {
                String id = image.getId();
                String url = image.getUrl();
                marketService.insertImg(code, id, userId);
            }
        }

        // 添加 market 数据
        marketService.add(market);
        return new ApiResponse("Market added successfully");
    }



    @PostMapping("/update")
    public ApiResponse marketUpdate(HttpServletRequest request,@RequestBody Market market){
        marketService.update(market);
        return new ApiResponse();
    }

    @PostMapping("/delete")
    ApiResponse marketDelete(HttpServletRequest request,@RequestBody Market market){
        marketService.delete(market.getId());
        return new ApiResponse();
    }

    @Operation(summary = "获取车位详情")
    @PostMapping(value = "/detail")
    public ApiResponse detail(@RequestBody Market market) {
        return marketService.detail(market.getId());
    }

}
