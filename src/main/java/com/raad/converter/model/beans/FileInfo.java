package com.raad.converter.model.beans;

import com.google.gson.Gson;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String s3path;
    private String fileName;

    public FileInfo() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getS3path() { return s3path; }
    public void setS3path(String s3path) { this.s3path = s3path; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    @Override
    public String toString() { return new Gson().toJson(this); }

}
