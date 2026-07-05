package com.ldz.park.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "用户登录请求参数")
public class LoginForm extends AbstractRequest {

    @Schema(description = "用户名/姓名（可选，部分登录方式使用）", example = "张三")
    private String name;

    @Schema(description = "登录账号（手机号或账号）", requiredMode = Schema.RequiredMode.REQUIRED, example = "18296325871")
    private String account;

    @Schema(description = "密码（账号密码登录时必填）", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String password;

    @Schema(description = "企业微信授权码（code），用于企业微信免密登录", example = "CODE_1234567890")
    private String code;

    @Schema(description = "企业微信用户ID（wxUserId），用于企业微信登录", example = "zhangsan")
    private String wxUserId;

    @Schema(description = "手机号（部分场景下与 account 一致，可冗余传入）", example = "18296325871")
    private String mobile;

    @Schema(description = "旧 token（小程序登录/续期时可携带，用于将旧 jti 拉黑）")
    private String token;
}