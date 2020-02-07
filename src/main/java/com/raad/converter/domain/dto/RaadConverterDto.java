package com.raad.converter.domain.dto;

import com.raad.converter.convergen.ScraperConstant;
import org.springframework.web.multipart.MultipartFile;

public class RaadConverterDto {

    private String inputType;
    private String outPutType = ScraperConstant.PDF;
    private MultipartFile file;

    public RaadConverterDto() { }

    public String getInputType() { return inputType; }
    public void setInputType(String inputType) { this.inputType = inputType; }

    public String getOutPutType() { return outPutType; }
    public void setOutPutType(String outPutType) { this.outPutType = outPutType; }

    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }


}
