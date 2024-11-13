package com.ldz.park.model.meta;

import java.io.Serializable;

public class ServerException  extends RuntimeException implements Serializable {
    private static final long serialVersionUID = -1520520889182518211L;
    private String error;
    private String message;

    public ServerException(){}

    public ServerException(String error) {
        this.error = error;
    }

    public ServerException(String error, String message) {
        this.error = error;
        this.message = message;

    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
