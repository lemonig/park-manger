package com.ldz.park.model.meta;

public enum  ErrorCode {

    /**
     * 服务端错误
     */
    API_ERROR("API_ERROR", 500),

    /**
     * 字段参数错误
     */
    FIELD_ERROR("FIELD_ERROR", 400),

    /**
     * 找不到资源或未授权
     */
    NOTFOUND_OR_UNAUTHORIZED("NOTFOUND_OR_UNAUTHORIZED", 404),

    /**
     * 未登录或授权过期
     */
    UNAUTHENTICATED("UNAUTHENTICATED", 401),

    /**
     * 用户访问的菜单找不到
     */
    PAGE404("PAGE404", 404);



    private final String code;
    private final int value;

    public String getCode() {
        return code;
    }

    /**
     * 数字业务码，用于统一响应体中的 code 字段（200=成功，4xx=客户端错误，5xx=服务端错误）
     */
    public int getValue() {
        return value;
    }

    private ErrorCode(String c, int v) {
        code = c;
        value = v;
    }

    /**
     * 字符串错误码转数字业务码；未登记的自定义错误码按参数错误 400 处理
     */
    public static int numericValueOf(String code) {
        for (ErrorCode e : values()) {
            if (e.code.equals(code)) {
                return e.value;
            }
        }
        return FIELD_ERROR.value;
    }
}
