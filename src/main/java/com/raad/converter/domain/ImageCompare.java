package com.raad.converter.domain;

import org.springframework.web.multipart.MultipartFile;

public class ImageCompare {

    private MultipartFile oldImage;
    private MultipartFile newImage;
    private Integer threshold;
    private Integer rectangleLineWidth;
    private Boolean fillDifferenceRectangles;
    private Double percentOpacityDifferenceRectangles;
    private Boolean fillExcludedRectangles;
    private Double percentOpacityExcludedRectangles;
    private Integer maximalRectangleCount;
    public Integer minimalRectangleSize;
    public Double pixelToleranceLevel;
    private Boolean drawExcludedRectangles;
    private Double allowingPercentOfDifferentPixels;

    public ImageCompare() { }

    public MultipartFile getOldImage() {
        return oldImage;
    }

    public void setOldImage(MultipartFile oldImage) {
        this.oldImage = oldImage;
    }

    public MultipartFile getNewImage() {
        return newImage;
    }

    public void setNewImage(MultipartFile newImage) {
        this.newImage = newImage;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Integer getRectangleLineWidth() {
        return rectangleLineWidth;
    }

    public void setRectangleLineWidth(Integer rectangleLineWidth) {
        this.rectangleLineWidth = rectangleLineWidth;
    }

    public Boolean getFillDifferenceRectangles() {
        return fillDifferenceRectangles;
    }

    public void setFillDifferenceRectangles(Boolean fillDifferenceRectangles) {
        this.fillDifferenceRectangles = fillDifferenceRectangles;
    }

    public Double getPercentOpacityDifferenceRectangles() {
        return percentOpacityDifferenceRectangles;
    }

    public void setPercentOpacityDifferenceRectangles(Double percentOpacityDifferenceRectangles) {
        this.percentOpacityDifferenceRectangles = percentOpacityDifferenceRectangles;
    }

    public Boolean getFillExcludedRectangles() {
        return fillExcludedRectangles;
    }

    public void setFillExcludedRectangles(Boolean fillExcludedRectangles) {
        this.fillExcludedRectangles = fillExcludedRectangles;
    }

    public Double getPercentOpacityExcludedRectangles() {
        return percentOpacityExcludedRectangles;
    }

    public void setPercentOpacityExcludedRectangles(Double percentOpacityExcludedRectangles) {
        this.percentOpacityExcludedRectangles = percentOpacityExcludedRectangles;
    }

    public Integer getMaximalRectangleCount() {
        return maximalRectangleCount;
    }

    public void setMaximalRectangleCount(Integer maximalRectangleCount) {
        this.maximalRectangleCount = maximalRectangleCount;
    }

    public Integer getMinimalRectangleSize() {
        return minimalRectangleSize;
    }

    public void setMinimalRectangleSize(Integer minimalRectangleSize) {
        this.minimalRectangleSize = minimalRectangleSize;
    }

    public Double getPixelToleranceLevel() {
        return pixelToleranceLevel;
    }

    public void setPixelToleranceLevel(Double pixelToleranceLevel) {
        this.pixelToleranceLevel = pixelToleranceLevel;
    }

    public Boolean getDrawExcludedRectangles() {
        return drawExcludedRectangles;
    }

    public void setDrawExcludedRectangles(Boolean drawExcludedRectangles) {
        this.drawExcludedRectangles = drawExcludedRectangles;
    }

    public Double getAllowingPercentOfDifferentPixels() {
        return allowingPercentOfDifferentPixels;
    }

    public void setAllowingPercentOfDifferentPixels(Double allowingPercentOfDifferentPixels) {
        this.allowingPercentOfDifferentPixels = allowingPercentOfDifferentPixels;
    }

}
