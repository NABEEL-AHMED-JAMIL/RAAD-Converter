package com.raad.converter.domain.dto;

import com.google.gson.Gson;

import java.util.List;

public class XmlMakerRequest {

    private String url;
    private List<TagInfo> tags;

    public XmlMakerRequest() { }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public List<TagInfo> getTags() { return tags; }
    public void setTags(List<TagInfo> tags) { this.tags = tags; }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
