package com.ldz.park.web.wechat;

import com.github.pagehelper.PageInfo;
import com.ldz.park.model.Market;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 小程序端车位收藏：全部接口走 JWT（userId 由拦截器注入 request），
 * 不加入白名单。
 */
@Tag(name = "微信小程序 / 车位收藏", description = "小程序端车位收藏")
@RestController
@RequestMapping("/api/wechat/favorite")
public class WechatFavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Operation(summary = "收藏车位（幂等，重复收藏不报错）")
    @PostMapping("/{marketId}")
    public ApiResponse<Void> add(@PathVariable Long marketId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("用户身份验证失败");
        }
        favoriteService.add(userId, marketId);
        return ApiResponse.success("收藏成功", null);
    }

    @Operation(summary = "取消收藏（幂等，未收藏也返回成功）")
    @DeleteMapping("/{marketId}")
    public ApiResponse<Void> remove(@PathVariable Long marketId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("用户身份验证失败");
        }
        favoriteService.remove(userId, marketId);
        return ApiResponse.success("已取消收藏", null);
    }

    @Operation(summary = "我的收藏列表（仅含在售车位）")
    @GetMapping("/list")
    public ApiResponse<List<Market>> list(
            @Parameter(description = "页码（最小1）") @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @Parameter(description = "每页大小（最小1）") @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("用户身份验证失败");
        }
        PageInfo<Market> pageInfo = favoriteService.list(userId, pageNum, pageSize);
        return ApiResponse.successWithPage(pageInfo.getList(), pageInfo);
    }

    @Operation(summary = "我收藏的车位ID集合（首页星标状态回显）")
    @GetMapping("/ids")
    public ApiResponse<List<Long>> ids(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("用户身份验证失败");
        }
        return ApiResponse.success(favoriteService.listIds(userId));
    }
}
