package com.raad.converter.domain;

import org.springframework.web.multipart.MultipartFile;

public class ImageProcessReqeust {

    private MultipartFile file;
    private String process;
    private Integer size;
    private Integer thresh;
    private Integer background;

    public ImageProcessReqeust() { }

    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }

    public String getProcess() { return process; }
    public void setProcess(String process) { this.process = process; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }

    public Integer getThresh() { return thresh; }
    public void setThresh(Integer thresh) { this.thresh = thresh; }

    public Integer getBackground() { return background; }
    public void setBackground(Integer background) { this.background = background; }


}
