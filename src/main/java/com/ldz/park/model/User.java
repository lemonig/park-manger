package com.ldz.park.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class User implements Serializable {
    private Integer id;
    private String name;
    private String account;
    private String doorplate;
    private String password;

    private String mobile;

    private Date gmtCreate;
    private Date gmtModify;
    private Date gmtActive;

}
