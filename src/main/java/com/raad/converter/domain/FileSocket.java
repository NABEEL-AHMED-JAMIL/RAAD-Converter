package com.raad.converter.domain;

import com.google.gson.Gson;

public class FileSocket {

    private Integer status;
    private String message;
    private String fileName;
    private byte[] download;

    public FileSocket() { }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public byte[] getDownload() { return download; }
    public void setDownload(byte[] download) { this.download = download; }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
