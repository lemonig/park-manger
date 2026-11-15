package com.ldz.park.web.wechat;

import com.ldz.park.model.Community;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 小程序端小区字典：发布/编辑车位时的小区选择器数据源。
 *
 * 定位参数仅用于「附近小区」排序展示，缺省时按 id 升序返回——
 * 人不在小区附近（异地发布）时搜索功能完全不受影响。
 */
@Tag(name = "微信小程序 / 小区字典", description = "小程序端小区选择器数据源")
@RestController
@RequestMapping("/api/wechat/community")
@RequiredArgsConstructor
public class WechatCommunityController {

    private final CommunityService communityService;

    @Operation(summary = "小区列表（仅启用，keyword 模糊；带 lat/lng 时按距离升序）")
    @GetMapping("/list")
    public ApiResponse<List<Community>> list(
            @Parameter(description = "小区名关键词（模糊）") @RequestParam(required = false) String keyword,
            @Parameter(description = "城市（精确）") @RequestParam(required = false) String city,
            @Parameter(description = "区/县（精确）") @RequestParam(required = false) String area,
            @Parameter(description = "定位纬度（仅用于附近排序，可选）") @RequestParam(required = false) Double lat,
            @Parameter(description = "定位经度（仅用于附近排序，可选）") @RequestParam(required = false) Double lng) {
        List<Community> list = communityService.listForPicker(keyword, city, area, lat, lng);
        return ApiResponse.success(list);
    }
}
