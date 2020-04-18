package com.raad.converter.domain;

import org.springframework.web.multipart.MultipartFile;

public class ImageCompare {

    private MultipartFile oldImage;
    private MultipartFile newImage;

    public ImageCompare() { }

    public MultipartFile getOldImage() { return oldImage; }
    public void setOldImage(MultipartFile oldImage) { this.oldImage = oldImage; }

    public MultipartFile getNewImage() { return newImage; }
    public void setNewImage(MultipartFile newImage) { this.newImage = newImage; }
}
