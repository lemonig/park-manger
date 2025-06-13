package com.ldz.park.web;

import com.ldz.park.model.Market;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.MarketService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    @Autowired
    MarketService marketService;

    @PostMapping(value = "/list")
    public ApiResponse marketList(HttpServletRequest request){
        return marketService.list();
    }

    @PostMapping(value = "/add")
    public ApiResponse marketAdd(HttpServletRequest request, @RequestBody Market market){
        String code = UUID.randomUUID().toString();
        market.setCode(code);

        List<Map<String, Object>> images = market.getPhoto();
        images.forEach(image -> {
            String id = (String) image.get("id");

            marketService.insetImg(code, id, );
        });


        marketService.add(market);
        return new ApiResponse();
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



}
