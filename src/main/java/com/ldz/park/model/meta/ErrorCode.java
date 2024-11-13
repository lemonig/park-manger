package com.ldz.park.model.meta;

public enum  ErrorCode {

    /**
     * 服务端错误
     */
    API_ERROR("API_ERROR"),

    /**
     * 字段参数错误
     */
    FIELD_ERROR("FIELD_ERROR"),

    /**
     * 找不到资源或未授权
     */
    NOTFOUND_OR_UNAUTHORIZED("NOTFOUND_OR_UNAUTHORIZED"),

    /**
     * 未登录或授权过期
     */
    UNAUTHENTICATED("UNAUTHENTICATED"),

    /**
     * 用户访问的菜单找不到
     */
    PAGE404("PAGE404");



    private String code;

    public String getCode() {
        return code;
    }

    private ErrorCode(String c) {
        code = c;
    }
}