package com.raad.converter.decoder;

public class TargetTag {

    public String url;
    // captcha tag image tag
    public String imageTag;
    // captcha input filed tag where paste the plan text
    public String inputTag;

    public TargetTag() { }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getImageTag() { return imageTag; }
    public void setImageTag(String imageTag) { this.imageTag = imageTag; }

    public String getInputTag() { return inputTag; }
    public void setInputTag(String inputTag) { this.inputTag = inputTag; }

}
