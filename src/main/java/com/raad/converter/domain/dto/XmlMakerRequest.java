package com.raad.converter.domain.dto;

import com.google.gson.Gson;

import java.util.List;

public class XmlMakerRequest {

    private String url;
    private List<TagInfo> tags;
    private Boolean screen_shoot;
    private Boolean html_cdata;
    private Boolean page_url;
    private Boolean pdf;
    private Boolean html;

    public XmlMakerRequest() { }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public List<TagInfo> getTags() { return tags; }
    public void setTags(List<TagInfo> tags) { this.tags = tags; }

    public Boolean getScreen_shoot() { return screen_shoot; }
    public void setScreen_shoot(Boolean screen_shoot) { this.screen_shoot = screen_shoot; }

    public Boolean getHtml_cdata() { return html_cdata; }
    public void setHtml_cdata(Boolean html_cdata) { this.html_cdata = html_cdata; }

    public Boolean getPage_url() { return page_url; }
    public void setPage_url(Boolean page_url) { this.page_url = page_url; }

    public Boolean getPdf() { return pdf; }
    public void setPdf(Boolean pdf) { this.pdf = pdf; }

    public Boolean getHtml() { return html; }
    public void setHtml(Boolean html) { this.html = html; }

    @Override
    public String toString() { return new Gson().toJson(this); }
}
