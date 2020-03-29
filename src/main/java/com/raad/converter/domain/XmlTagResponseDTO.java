package com.raad.converter.domain;

import com.google.gson.Gson;

import java.util.List;
import java.util.Set;


public class XmlTagResponseDTO {

    private Set<String> xmlNode;
    private Set<TableTagDetail> tagDetails;
    private List<TagInfo> tagInfos;

    public XmlTagResponseDTO() { }

    public Set<String> getXmlNode() { return xmlNode; }
    public void setXmlNode(Set<String> xmlNode) { this.xmlNode = xmlNode; }

    public Set<TableTagDetail> getTagDetails() { return tagDetails; }
    public void setTagDetails(Set<TableTagDetail> tagDetails) { this.tagDetails = tagDetails; }

    public List<TagInfo> getTagInfos() { return tagInfos; }
    public void setTagInfos(List<TagInfo> tagInfos) { this.tagInfos = tagInfos; }

    @Override
    public String toString() { return new Gson().toJson(this); }
}