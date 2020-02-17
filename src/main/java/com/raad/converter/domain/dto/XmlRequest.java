package com.raad.converter.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import org.springframework.web.multipart.MultipartFile;

//http://xsd2xml.com/
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class XmlRequest {

    private String url;
    private MultipartFile file;
    private MultipartFile xmlFile;
    private MultipartFile schemaFile;

    public XmlRequest() { }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }

    public MultipartFile getXmlFile() { return xmlFile; }
    public void setXmlFile(MultipartFile xmlFile) { this.xmlFile = xmlFile; }

    public MultipartFile getSchemaFile() { return schemaFile; }
    public void setSchemaFile(MultipartFile schemaFile) { this.schemaFile = schemaFile; }

    @Override
    public String toString() { return new Gson().toJson(this); }

}
