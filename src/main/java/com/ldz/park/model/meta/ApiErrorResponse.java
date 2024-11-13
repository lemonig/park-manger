package com.ldz.park.model.meta;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;


import javax.servlet.http.HttpServletRequest;
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
        this.error = ex.getError();
        this.message = ex.getMessage();
        this.path = request.getRequestURI();
    }

    public ApiErrorResponse(Exception ex, HttpServletRequest request) {
        this.error = ErrorCode.API_ERROR.getCode();
        this.exception = ex.getClass().getSimpleName();
        this.message = ex.getMessage();
        this.path = request.getRequestURI();
    }
}
