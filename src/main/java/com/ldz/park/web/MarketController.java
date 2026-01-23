package com.ldz.park.web;

import com.github.pagehelper.PageInfo;
import com.ldz.park.model.Market;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.MarketService;
import com.ldz.park.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 车位租售管理 Controller（优化版）
 *
 * 优化说明：
 * 1. RESTful 标准 HTTP 方法：
 * - GET 用于查询（detail/list），安全、幂等、可缓存。
 * - POST 用于新增。
 * - PUT 用于更新（全量更新，幂等）。
 * - DELETE 用于删除。
 * 2. 参数传递优化：
 * - id 用 @PathVariable（路径变量，更清晰、标准）。
 * - 复杂对象用 @RequestBody + @Valid 验证。
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

    @Autowired
    private JwtUtil jwtUtil; // 注入工具类

    /**
     * 查询列表（支持分页 + 条件）
     */
    /**
     * 查询车位列表（分页 + 条件查询）
     */
    @Operation(summary = "查询车位列表", description = "支持分页、租售类型、车位编号模糊查询、审核状态过滤，返回已填充图片的完整数据")
    @GetMapping("/list")
    public ApiResponse<List<Market>> list(
            @Parameter(description = "页码（最小1）") @RequestParam(defaultValue = "1") @Min(1) int pageNum,

            @Parameter(description = "每页大小（最小1，推荐10-50）") @RequestParam(defaultValue = "10") @Min(1) int pageSize,

            @Parameter(description = "租售类型：1=出售，2=租赁（可选）") @RequestParam(required = false) Integer type,

            @Parameter(description = "车位编号模糊查询（如 'A-101'，可选）") @RequestParam(required = false) String parkingNo,

            @Parameter(description = "审核状态：0=待审核，1=通过，2=拒绝（可选）") @RequestParam(required = false) Integer status) {

        // 构建查询条件对象
        Market query = new Market();
        query.setType(type);
        query.setParkingNo(parkingNo);
        query.setStatus(status);

        // 调用 Service（内部已处理分页 + 批量图片填充）
        PageInfo<Market> pageInfo = marketService.list(query, pageNum, pageSize);

        // 返回数据列表和分页信息，data 存放列表，page 与 code、data 同级
        return ApiResponse.successWithPage(pageInfo.getList(), pageInfo);
    }

    /**
     * 查询详情
     */
    @Operation(summary = "车位详情查询")
    @GetMapping("/detail/{id}")
    public ApiResponse<Market> detail(@Parameter(description = "车位ID") @PathVariable Long id) {
        Market market = marketService.detail(id); // 这里需要看下 传参的格式不太对
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
    public ApiResponse<Void> add(@Valid @RequestBody Market market, HttpServletRequest request) {
        // 验证用户身份
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.error(401, "未授权访问");
        }

        String token = authHeader.substring(7);
        if (jwtUtil.isTokenExpired(token)) {
            return ApiResponse.error(401, "登录已过期，请重新登录");
        }

        // 从 JWT token 获取用户ID
        Integer currentUserId = jwtUtil.extractUserId(token);
        if (currentUserId == null) {
            return ApiResponse.error(401, "用户身份验证失败");
        }

        // 设置发布用户ID（后端自动设置，不从前端接收）
        market.setUserId(currentUserId);

        marketService.add(market, currentUserId);
        return ApiResponse.success("车位信息发布成功", null);
    }

    /**
     * 更新车位
     */
    @Operation(summary = "更新车位信息")
    @PutMapping("/update")
    public ApiResponse<Void> update(@Valid @RequestBody Market market, BindingResult bindingResult,
            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return ApiResponse.error(400, bindingResult.getFieldError().getDefaultMessage());
        }

        if (market.getId() == null) {
            return ApiResponse.error(400, "更新时ID不能为空");
        }

        // 验证用户身份
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.error(401, "未授权访问");
        }

        String token = authHeader.substring(7);
        if (jwtUtil.isTokenExpired(token)) {
            return ApiResponse.error(401, "登录已过期，请重新登录");
        }

        // 从 JWT token 获取用户ID
        Integer currentUserId = jwtUtil.extractUserId(token);
        if (currentUserId == null) {
            return ApiResponse.error(401, "用户身份验证失败");
        }

        marketService.update(market, currentUserId);
        return ApiResponse.success("车位信息更新成功", null);
    }

    /**
     * 删除车位
     */
    @Operation(summary = "删除车位信息")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> delete(@Parameter(description = "车位ID") @PathVariable Long id) {
        marketService.delete(id);
        return ApiResponse.success();
    }
}