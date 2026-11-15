package com.ldz.park.web.admin;

import com.github.pagehelper.PageInfo;
import com.ldz.park.model.Community;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.CommunityService;
import com.ldz.park.service.TencentMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台小区库管理：运营建库的主入口。
 *
 * POI 搜索代理（/poi/search）供运营按「城市+关键词」搜腾讯地图真实 POI，
 * 确认后入库，保证名称规范、坐标正确；地图 key 不出服务端。
 */
@Tag(name = "后台管理 / 小区字典", description = "后台管理端小区库维护")
@RestController
@RequestMapping("/api/admin/community")
@RequiredArgsConstructor
public class AdminCommunityController {

    private final CommunityService communityService;
    private final TencentMapService tencentMapService;

    @Operation(summary = "小区列表（分页，含停用）")
    @GetMapping("/list")
    public ApiResponse<List<Community>> list(
            @Parameter(description = "小区名关键词（模糊）") @RequestParam(required = false) String keyword,
            @Parameter(description = "城市（精确）") @RequestParam(required = false) String city,
            @Parameter(description = "区/县（精确）") @RequestParam(required = false) String area,
            @Parameter(description = "状态：1=启用，0=停用（可选）") @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageInfo<Community> pageInfo = communityService.listAdmin(keyword, city, area, status, pageNum, pageSize);
        return ApiResponse.successWithPage(pageInfo.getList(), pageInfo);
    }

    @Operation(summary = "小区详情")
    @GetMapping("/{id}")
    public ApiResponse<Community> detail(@PathVariable Long id) {
        Community community = communityService.detail(id);
        if (community == null) {
            return ApiResponse.notFound("小区不存在");
        }
        return ApiResponse.success(community);
    }

    @Operation(summary = "新增小区")
    @PostMapping
    public ApiResponse<Void> add(@RequestBody Community community) {
        communityService.add(community);
        return ApiResponse.ok();
    }

    @Operation(summary = "更新小区")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Community community) {
        community.setId(id);
        communityService.update(community);
        return ApiResponse.ok();
    }

    @Operation(summary = "删除小区（被车位引用时不可删，可改停用）")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        communityService.delete(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "腾讯地图 POI 搜索（建库辅助，城市必填）")
    @GetMapping("/poi/search")
    public ApiResponse<List<Community>> poiSearch(
            @Parameter(description = "POI 名称关键词") @RequestParam String keyword,
            @Parameter(description = "城市，如 广州") @RequestParam String city) {
        if (keyword == null || keyword.isBlank() || city == null || city.isBlank()) {
            return ApiResponse.badRequest("关键词与城市不能为空");
        }
        return ApiResponse.success(tencentMapService.searchPoi(keyword.trim(), city.trim()));
    }
}
