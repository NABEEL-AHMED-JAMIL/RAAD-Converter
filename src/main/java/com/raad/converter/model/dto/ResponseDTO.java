package com.raad.converter.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.raad.converter.model.enums.ApiCode;


@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO {

    private ApiCode status;
    private String message;
    private Object data;

    public ResponseDTO() { }

    public ResponseDTO(ApiCode status, String message) {
        this.status = status;
        this.message = message;
    }

    public ResponseDTO(String message, Object data) {
        this.message = message;
        this.data = data;
    }

    public ResponseDTO(ApiCode status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ApiCode getStatus() { return status; }
    public void setStatus(ApiCode status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
