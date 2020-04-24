package com.raad.converter.domain;

import com.google.gson.Gson;

public class FileInfoDto {

    private String s3path;
    private String file_name;

    public String getS3path() { return s3path; }
    public void setS3path(String s3path) { this.s3path = s3path; }

    public String getFile_name() { return file_name; }
    public void setFile_name(String file_name) { this.file_name = file_name; }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
