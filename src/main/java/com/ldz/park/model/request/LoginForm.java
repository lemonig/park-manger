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

    @Schema(description = "用户名/昵称（可选，仅在首次静默登录建号或昵称回填时使用）", example = "张三")
    private String name;

    @Schema(description = "登录账号（账号密码登录时必填；小程序静默登录不使用）", example = "18296325871")
    private String account;

    @Schema(description = "密码（账号密码登录时必填；小程序静默登录不使用）", example = "123456")
    private String password;

    @Schema(description = "微信小程序登录 code（wx.login 返回，用于 jscode2session 换取 openid；小程序静默登录时必填）", example = "0a1b2c3d4e5f6g7h8i9j")
    private String code;

    /**
     * @deprecated 服务端不再读取，仅为字段兼容保留。企业微信 SSO 未实现。
     */
    @Deprecated
    @Schema(description = "已废弃，服务端不再读取（保留字段以兼容旧客户端）", deprecated = true)
    private String wxUserId;

    @Schema(description = "手机号（可选，小程序静默登录时若首次建号可回填，或用于按手机号回查已有用户并绑定 openid）", example = "18296325871")
    private String mobile;

    @Schema(description = "旧 token（可选，小程序静默登录/续期时携带，用于将旧 jti 加入黑名单，避免多 token 并存）")
    private String token;
}