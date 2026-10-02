package com.ldz.park.model.meta;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;


import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ApiErrorResponse extends ApiResponse implements Serializable {

    private static final long serialVersionUID = -5011314791279335808L;
    private Boolean success=false;

    private String error;
    private String exception;
    private String message;
    private String path;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    public ApiErrorResponse(ServerException ex, HttpServletRequest request) {
        // 业务错误码必须写入 code，避免沿用父类默认的 200 被客户端误判为成功
        this.setCode(ErrorCode.numericValueOf(ex.getError()));
        this.error = ex.getError();
        this.message = ex.getMessage();
        this.path = request.getRequestURI();
    }

    public ApiErrorResponse(Exception ex, HttpServletRequest request) {
        this.setCode(ErrorCode.API_ERROR.getValue());
        this.error = ErrorCode.API_ERROR.getCode();
        this.exception = ex.getClass().getSimpleName();
        this.message = ex.getMessage();
        this.path = request.getRequestURI();
    }
}
