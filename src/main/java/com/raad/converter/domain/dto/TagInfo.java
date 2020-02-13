package com.raad.converter.domain.dto;

import com.google.gson.Gson;

public class TagInfo {

    private String tag_name;
    private String parent_tag;
    private String html_tag;

    public TagInfo() { }

    public String getTag_name() { return tag_name; }
    public void setTag_name(String tag_name) { this.tag_name = tag_name; }

    public String getParent_tag() { return parent_tag; }
    public void setParent_tag(String parent_tag) { this.parent_tag = parent_tag; }

    public String getHtml_tag() { return html_tag; }
    public void setHtml_tag(String html_tag) { this.html_tag = html_tag; }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
