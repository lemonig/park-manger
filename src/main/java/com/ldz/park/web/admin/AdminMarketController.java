package com.ldz.park.web.admin;

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

@Tag(name = "后台管理 / 车位租售", description = "后台管理端车位租售管理")
@RestController
@RequestMapping("/api/admin/market")
public class AdminMarketController {

    @Autowired
    private MarketService marketService;

    @Operation(summary = "车位列表（全量，含所有 status）")
    @GetMapping("/list")
    public ApiResponse<List<Market>> list(
            @Parameter(description = "页码（最小1）") @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @Parameter(description = "每页大小（最小1）") @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @Parameter(description = "租售类型（可选）") @RequestParam(required = false) Integer type,
            @Parameter(description = "车位编号（模糊，可选）") @RequestParam(required = false) String parkingNo,
            @Parameter(description = "审核状态（可选）") @RequestParam(required = false) Integer status) {
        Market query = new Market();
        query.setType(type);
        query.setParkingNo(parkingNo);
        query.setStatus(status);
        PageInfo<Market> pageInfo = marketService.list(query, pageNum, pageSize);
        return ApiResponse.successWithPage(pageInfo.getList(), pageInfo);
    }

    @Operation(summary = "车位详情")
    @GetMapping("/detail/{id}")
    public ApiResponse<Market> detail(@PathVariable Long id) {
        Market market = marketService.detail(id);
        if (market == null) {
            return ApiResponse.notFound("车位信息不存在");
        }
        return ApiResponse.success(market);
    }

    @Operation(summary = "新增车位（后台代发布）")
    @PostMapping
    public ApiResponse<Void> add(@Valid @RequestBody Market market, HttpServletRequest request) {
        Integer adminId = (Integer) request.getAttribute("userId");
        if (market.getUserId() == null) {
            market.setUserId(adminId);
        }
        marketService.add(market, adminId);
        return ApiResponse.success("车位信息发布成功", null);
    }

    @Operation(summary = "更新车位")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody Market market) {
        market.setId(id);
        marketService.update(market);
        return ApiResponse.success("车位信息更新成功", null);
    }

    @Operation(summary = "删除车位")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        marketService.delete(id);
        return ApiResponse.success();
    }

    @Operation(summary = "审核车位（1=通过，2=拒绝）")
    @PostMapping("/{id}/audit")
    public ApiResponse<Void> audit(@PathVariable Long id, @RequestBody AuditForm form) {
        if (form == null || form.getStatus() == null) {
            return ApiResponse.badRequest("审核状态不能为空");
        }
        marketService.audit(id, form.getStatus());
        return ApiResponse.ok();
    }

    public static class AuditForm {
        private Integer status;
        private String remark;

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
