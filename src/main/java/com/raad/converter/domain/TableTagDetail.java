package com.raad.converter.domain;

import com.google.gson.Gson;

import java.util.Objects;

public class TableTagDetail {

    private String tagKey;
    private String tagValue;

    public TableTagDetail() { }

    public String getTagKey() { return tagKey; }
    public void setTagKey(String tagKey) { this.tagKey = tagKey; }

    public String getTagValue() { return tagValue; }
    public void setTagValue(String tagValue) { this.tagValue = tagValue; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableTagDetail tableTagDetail = (TableTagDetail) o;
        return tagKey.equals(tableTagDetail.tagKey) &&
                tagValue.equals(tableTagDetail.tagValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagKey, tagValue);
    }

    @Override
    public String toString() { return new Gson().toJson(this); }
}