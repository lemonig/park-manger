package com.ldz.park.web.wechat;

import com.github.pagehelper.PageInfo;
import com.ldz.park.model.Market;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.MarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
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

@Tag(name = "微信小程序 / 车位租售", description = "小程序端车位租售信息")
@RestController
@RequestMapping("/api/wechat/market")
public class WechatMarketController {

    @Autowired
    private MarketService marketService;

    @Operation(summary = "车位列表（仅已审核通过）")
    @GetMapping("/list")
    public ApiResponse<List<Market>> list(
            @Parameter(description = "页码（最小1）") @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @Parameter(description = "每页大小（最小1）") @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @Parameter(description = "租售类型：1=出售，2=租赁（可选）") @RequestParam(required = false) Integer type,
            @Parameter(description = "车位编号模糊查询（可选）") @RequestParam(required = false) String parkingNo) {
        Market query = new Market();
        query.setType(type);
        query.setParkingNo(parkingNo);
        PageInfo<Market> pageInfo = marketService.listPublished(query, pageNum, pageSize);
        return ApiResponse.successWithPage(pageInfo.getList(), pageInfo);
    }

    @Operation(summary = "我发布的车位")
    @GetMapping("/mine")
    public ApiResponse<List<Market>> mine(
            @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("用户身份验证失败");
        }
        Market query = new Market();
        query.setType(type);
        query.setStatus(status);
        PageInfo<Market> pageInfo = marketService.listByUser(userId, query, pageNum, pageSize);
        return ApiResponse.successWithPage(pageInfo.getList(), pageInfo);
    }

    @Operation(summary = "车位详情")
    @GetMapping("/detail/{id}")
    public ApiResponse<Market> detail(@PathVariable Long id, HttpServletRequest request) {
        Market market = marketService.detail(id);
        if (market == null) {
            return ApiResponse.notFound("车位信息不存在");
        }
        Integer userId = (Integer) request.getAttribute("userId");
        boolean owner = userId != null && userId.equals(market.getUserId());
        boolean approved = market.getStatus() != null && market.getStatus() == 1;
        if (!approved && !owner) {
            return ApiResponse.forbidden("无权限查看该车位信息");
        }
        return ApiResponse.success(market);
    }

    @Operation(summary = "发布车位")
    @PostMapping
    public ApiResponse<Void> add(@Valid @RequestBody Market market, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("用户身份验证失败");
        }
        market.setUserId(userId);
        market.setStatus(0);
        marketService.add(market, userId);
        return ApiResponse.success("车位信息发布成功", null);
    }

    @Operation(summary = "更新本人车位")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody Market market,
                                    HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("用户身份验证失败");
        }
        market.setId(id);
        marketService.updateByOwner(market, userId);
        return ApiResponse.success("车位信息更新成功", null);
    }

    @Operation(summary = "撤回/删除本人车位")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("用户身份验证失败");
        }
        marketService.deleteByOwner(id, userId);
        return ApiResponse.success();
    }
}
