package com.raad.converter.domain;

import com.google.gson.Gson;

public class ResponseDTO {

    private String text;
    private String message;
    private Object data;
    private Object ext;

    public ResponseDTO() { }

    public ResponseDTO(String message, Object data) {
        this.message = message;
        this.data = data;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public Object getExt() { return ext; }
    public void setExt(Object ext) { this.ext = ext; }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
