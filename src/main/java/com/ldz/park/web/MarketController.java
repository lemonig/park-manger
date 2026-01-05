package com.ldz.park.web;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ldz.park.model.Carport;
import com.ldz.park.model.Market;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.model.vo.market.SimpleImage;
import com.ldz.park.service.MarketService;
import com.ldz.park.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 车位租售管理 Controller（优化版）
 *
 * 优化说明：
 * 1. RESTful 标准 HTTP 方法：
 *    - GET 用于查询（detail/list），安全、幂等、可缓存。
 *    - POST 用于新增。
 *    - PUT 用于更新（全量更新，幂等）。
 *    - DELETE 用于删除。
 * 2. 参数传递优化：
 *    - id 用 @PathVariable（路径变量，更清晰、标准）。
 *    - 复杂对象用 @RequestBody + @Valid 验证。
 * 3. 新增 list 接口（带分页 + 条件查询），使用 PageHelper（与您的 pom 兼容）。
 * 4. 统一返回 ApiResponse，成功时带消息，失败时全局异常处理器可捕获（建议后期加）。
 * 5. Swagger 注解完善（Tag + Operation + Parameter），文档更专业。
 * 6. 验证 + 错误处理：@Valid + BindingResult（简单实现，生产建议全局异常）。
 * 7. 代码结构清晰、可维护、扩展性强（后期加权限、日志、图片处理）。
 * 8. 移除无用 import（如 HttpServletRequest、UUID）。
 */
@Tag(name = "车位租售管理 API")
@RestController
@RequestMapping("/api/market")
public class MarketController {

    @Autowired
    private MarketService marketService;

    /**
     * 查询列表（支持分页 + 条件）
     */
    @Operation(summary = "车位列表查询", description = "支持分页、租售类型、车位编号模糊、审核状态过滤")
    @GetMapping("/list")
    public ApiResponse<PageInfo<Market>> list(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小，默认10") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "租售类型：1出售 2租赁") @RequestParam(required = false) Integer type,
            @Parameter(description = "车位编号模糊查询") @RequestParam(required = false) String parkingNo,
            @Parameter(description = "审核状态：0待审核 1通过 2拒绝") @RequestParam(required = false) Integer status) {

        // PageHelper 分页
        PageHelper.startPage(pageNum, pageSize);

        // 构建条件（假设 service 支持 Market query 参数）
        Market query = new Market();
        if (type != null) query.setType(type);
        if (parkingNo != null && !parkingNo.trim().isEmpty()) query.setParkingNo(parkingNo);
        if (status != null) query.setStatus(status);

        List<Market> list = marketService.list(query);
        PageInfo<Market> pageInfo = new PageInfo<>(list);

        return ApiResponse.success(pageInfo);
    }

    /**
     * 查询详情
     */
    @Operation(summary = "车位详情查询")
    @GetMapping("/detail/{id}")
    public ApiResponse<Market> detail(@Parameter(description = "车位ID") @PathVariable Long id) {
        Market market = marketService.detail(id);
        if (market == null) {
            return ApiResponse.error(404, "车位信息不存在");
        }
        return ApiResponse.success(market);
    }

    /**
     * 新增车位
     */
    @Operation(summary = "新增车位信息")
    @PostMapping("/add")
    public ApiResponse<Void> add(@Valid @RequestBody Market market, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ApiResponse.error(400, bindingResult.getFieldError().getDefaultMessage());
        }
        marketService.add(market);
        return ApiResponse.success("添加成功");
    }

    /**
     * 更新车位
     */
    @Operation(summary = "更新车位信息")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody Market market, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ApiResponse.error(400, bindingResult.getFieldError().getDefaultMessage());
        }
        if (market.getId() == null) {
            return ApiResponse.error(400, "更新时ID不能为空");
        }
        marketService.update(market);
        return ApiResponse.success("更新成功");
    }

    /**
     * 删除车位
     */
    @Operation(summary = "删除车位信息")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> delete(@Parameter(description = "车位ID") @PathVariable Long id) {
        marketService.delete(id);
        return ApiResponse.success("删除成功");
    }
}