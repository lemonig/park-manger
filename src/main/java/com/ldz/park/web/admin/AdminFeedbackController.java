package com.ldz.park.web.admin;

import com.github.pagehelper.PageInfo;
import com.ldz.park.model.Feedback;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "后台管理 / 意见反馈", description = "后台管理端意见反馈查看与处理")
@RestController
@RequestMapping("/api/admin/feedback")
public class AdminFeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Operation(summary = "反馈列表（分页，含提交人信息与图片）")
    @GetMapping("/list")
    public ApiResponse<List<Feedback>> list(
            @Parameter(description = "页码（最小1）") @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @Parameter(description = "每页大小（最小1）") @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @Parameter(description = "处理状态：0=待处理，1=已处理（可选）") @RequestParam(required = false) Integer status,
            @Parameter(description = "反馈类型（字典 feedback_type 的 value，可选）") @RequestParam(required = false) String type,
            @Parameter(description = "关键字（内容/联系方式/提交人，模糊，可选）") @RequestParam(required = false) String keyword,
            @Parameter(description = "提交时间-起（yyyy-MM-dd HH:mm:ss，可选）") @RequestParam(required = false) String startTime,
            @Parameter(description = "提交时间-止（yyyy-MM-dd HH:mm:ss，可选）") @RequestParam(required = false) String endTime) {
        Feedback query = new Feedback();
        query.setStatus(status);
        query.setType(type);
        query.setKeyword(keyword);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        PageInfo<Feedback> pageInfo = feedbackService.list(query, pageNum, pageSize);
        return ApiResponse.successWithPage(pageInfo.getList(), pageInfo);
    }

    @Operation(summary = "反馈详情（含图片）")
    @GetMapping("/detail/{id}")
    public ApiResponse<Feedback> detail(@PathVariable Long id) {
        Feedback feedback = feedbackService.detail(id);
        if (feedback == null) {
            return ApiResponse.notFound("反馈不存在");
        }
        return ApiResponse.success(feedback);
    }

    @Operation(summary = "标记已处理（handleRemark 必填，仅待处理状态可处理）")
    @PutMapping("/{id}/handle")
    public ApiResponse<Void> handle(@PathVariable Long id, @RequestBody HandleForm form,
                                    HttpServletRequest request) {
        Integer adminId = (Integer) request.getAttribute("userId");
        if (adminId == null) {
            return ApiResponse.unauthorized("用户身份验证失败");
        }
        if (form == null) {
            return ApiResponse.badRequest("处理备注不能为空");
        }
        feedbackService.handle(id, form.getHandleRemark(), adminId);
        return ApiResponse.success("已标记为已处理", null);
    }

    public static class HandleForm {
        private String handleRemark;

        public String getHandleRemark() {
            return handleRemark;
        }

        public void setHandleRemark(String handleRemark) {
            this.handleRemark = handleRemark;
        }
    }
}
