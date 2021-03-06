package com.raad.converter.domain;

import com.google.gson.Gson;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class FilePath {

    @NotBlank
    @NotNull
    private String path;

    public FilePath() { }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    @Override
    public String toString() { return new Gson().toJson(this); }

}
