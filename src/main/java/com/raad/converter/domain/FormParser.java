package com.raad.converter.domain;

import com.google.gson.Gson;

public class FormParser {

    private String url;
    private String tag;

    public FormParser() { }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
