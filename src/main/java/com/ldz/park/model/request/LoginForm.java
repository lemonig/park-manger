package com.ldz.park.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class LoginForm extends AbstractRequest{

    private String  name;

    @ApiModelProperty(value  = "手机号码",required = true, example = "18296325871")
    private String  Account;

    @ApiModelProperty(value = "密码",required = true,example = "123456")
    private String  password;

    @ApiModelProperty(value  = "前端企业微信授权码")
    private String code;

    @ApiModelProperty(value  = "企业微信号")
    private String wxUserId;

    private String mobile;
}
