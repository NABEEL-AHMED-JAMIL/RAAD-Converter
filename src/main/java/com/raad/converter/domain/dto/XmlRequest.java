package com.raad.converter.domain.dto;

import com.google.gson.Gson;
import org.springframework.web.multipart.MultipartFile;


public class XmlRequest {

    private String url;
    private MultipartFile file;

    public XmlRequest() { }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }

    @Override
    public String toString() { return new Gson().toJson(this); }

}
