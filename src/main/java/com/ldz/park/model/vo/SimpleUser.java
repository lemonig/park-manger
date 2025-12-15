package com.ldz.park.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ldz.park.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "简化版用户信息，通常用于登录返回、个人中心展示或用户列表")
public class SimpleUser implements Serializable {

    @Schema(description = "用户ID", example = "1001")
    private Integer id;

    @Schema(description = "姓名/昵称", example = "张三")
    private String nickname;

    @Schema(description = "登录Token（仅在登录成功时返回）", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.x...")
    private String token;

    @Schema(description = "头像URL", example = "https://example.com/avatar/1001.jpg")
    private String avatar;

    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    @Schema(description = "个人简介/描述", example = "热爱停车位管理")
    private String description;

    // 以下字段根据实际业务需求可选返回（目前不常用，可视情况开启）
    // private String doorplate;   // 门牌号（如车位相关）
    // private String account;     // 账号（登录名），一般不返回给前端，避免泄露

    /**
     * 从 User 实体转换为 SimpleUser（不带 token，适用于用户列表、个人信息查询等场景）
     */
    public SimpleUser fromUser(User user, String token){
        SimpleUser response = fromUser(user);
        response.setToken(token);
        return response;
    }

    public SimpleUser fromUser(User user){
        SimpleUser response = new SimpleUser();
        response.setId(user.getId());
        response.setNickname(user.getName());
        response.setMobile(user.getMobile());
        return response;
    }

    public SimpleUser fromUserSimple(User user){
        SimpleUser response = new SimpleUser();
        response.setId(user.getId());
        response.setNickname(user.getName());
        response.setMobile(user.getMobile());
        return response;
    }
}