package com.raad.converter.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileDetail {

    private List<Long> ids;
    private String toke;

    public FileDetail() { }

    public List<Long> getIds() { return ids; }
    public void setIds(List<Long> ids) { this.ids = ids; }

    public String getToke() { return toke; }
    public void setToke(String toke) { this.toke = toke; }

    @Override
    public String toString() { return new Gson().toJson(this); }

}
