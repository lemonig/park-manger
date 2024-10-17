package com.ldz.park.model.meta;

import lombok.Data;
import org.joda.time.DateTime;

import java.io.Serializable;

@Data
public class ApiResponse implements Serializable {
    private Boolean success = true;
    private Object data;
    private Object additional_data;
    private Object related_objects;
    private  String message;
    private String request_time = new DateTime().toString();

    public ApiResponse(){
    
    }
    public ApiResponse (Object data){
        this.data = data;
    }

}
