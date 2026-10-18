package com.ldz.park.web.wechat;

import com.ldz.park.model.Feedback;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "微信小程序 / 意见反馈", description = "小程序端意见反馈提交")
@RestController
@RequestMapping("/api/wechat/feedback")
public class WechatFeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Operation(summary = "提交意见反馈（userId 从 JWT 取；type 为字典 feedback_type 的 value；images 传上传返回的 {id, url} 列表，最多 4 张）")
    @PostMapping
    public ApiResponse<Void> add(@RequestBody Feedback feedback, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("用户身份验证失败");
        }
        feedbackService.add(feedback, userId);
        return ApiResponse.success("反馈提交成功，感谢你的意见", null);
    }
}
