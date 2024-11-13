package com.ldz.park.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ldz.park.model.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description="密码")
public class SimpleUser {

    private Integer id;

    @ApiModelProperty(notes = "昵称")
    private String nickname;

    @ApiModelProperty(notes = "token")
    private String token;

    @ApiModelProperty(notes = "头像")
    private String avatar;
    private String title;

    private String pinyin;
    private String jianpin;
    private String firstchar;

    private String alias;

    private String mobile;

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