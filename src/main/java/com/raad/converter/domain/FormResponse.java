package com.raad.converter.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormResponse {

    private String controlName;
    private String controlType;
    private String htmlTag;
    private Object values;
    private Object ext;

    public FormResponse() { }

    public String getControlName() { return controlName; }
    public void setControlName(String controlName) { this.controlName = controlName; }

    public String getControlType() { return controlType; }
    public void setControlType(String controlType) { this.controlType = controlType; }

    public String getHtmlTag() { return htmlTag; }
    public void setHtmlTag(String htmlTag) { this.htmlTag = htmlTag; }

    public Object getValues() { return values; }
    public void setValues(Object values) { this.values = values; }

    public Object getExt() { return ext; }
    public void setExt(Object ext) { this.ext = ext; }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
