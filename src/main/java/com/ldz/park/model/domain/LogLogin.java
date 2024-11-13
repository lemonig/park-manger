package com.ldz.park.model.domain;

import lombok.Data;

@Data
public class LogLogin extends AbstractReportForm{

    private String nickname;
    private Integer user_id;

    /**
     * 1:账号 2：微信
     */
    private Integer channel;
    private String mobile;

    /**
     * 1: 登录 2 登出
     */
    private Integer type;
}
