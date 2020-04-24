package com.raad.converter.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.google.gson.Gson;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "files", "op_file_name" })
public class FileWithObject {

    @JsonProperty("files")
    private List<MultipartFile> files;
    @JsonRawValue
    @JsonProperty("op_file_name")
    private String op_file_name;

    public List<MultipartFile> getFiles() { return files; }
    public void setFiles(List<MultipartFile> files) { this.files = files; }

    public String getOp_file_name() { return op_file_name; }
    public void setOp_file_name(String op_file_name) { this.op_file_name = op_file_name; }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
