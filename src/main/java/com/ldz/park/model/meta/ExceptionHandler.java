package com.ldz.park.model.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
public class ExceptionHandler {
    static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    @org.springframework.web.bind.annotation.ExceptionHandler(value = ServerException.class)
    @ResponseBody
    public ApiErrorResponse handleInputException(ServerException ex, HttpServletRequest request) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(ex, request);
        return apiErrorResponse;
    }

    /**
     * 处理无法捕捉的异常
     * @param ex
     * @param request
     * @return
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ApiErrorResponse handleException(Exception ex, HttpServletRequest request) {
        logger.error("Ops!", ex);
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(ex, request);
        return apiErrorResponse;
    }

}
