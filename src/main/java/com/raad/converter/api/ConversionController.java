package com.raad.converter.api;

import com.raad.converter.convergen.RaadStreamConverter;
import com.raad.converter.convergen.ScraperConstant;
import com.raad.converter.util.LocalFileHandler;
import io.swagger.annotations.Api;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;


@RestController
@RequestMapping("/conversion")
@Api(tags = {"RAAD-Conversion := RAAD-Conversion EndPoint"})
public class ConversionController {

    public Logger logger = LogManager.getLogger(ConversionController.class);

    private String PDF_STORE = "pdf";

    @Autowired
    private RaadStreamConverter raadStreamConverter;
    @Autowired
    private LocalFileHandler localFileHandler;

    @PostConstruct
    public void init() {
        localFileHandler.makeDir(File.separator+PDF_STORE);
    }

    @RequestMapping(path = "file-converter/v1", method = RequestMethod.POST)
    public ResponseEntity<?> convertPdf(@RequestParam("file") final MultipartFile multipart) throws Exception {
        logger.info("File Content Type :- " + multipart.getContentType());
        String fileName = FilenameUtils.getBaseName(multipart.getOriginalFilename());
        String targetFilename = String.format("%s%s", fileName, ScraperConstant.PDF_EXTENSION);
        // will think to take the file in dir or not delete
        ByteArrayOutputStream convertedFile = this.raadStreamConverter.doConvert(multipart.getInputStream(), multipart.getOriginalFilename(), targetFilename);
        // after convert successfully store the pdf file
        this.localFileHandler.saveFile(convertedFile, File.separator+PDF_STORE+File.separator+targetFilename);
        final HttpHeaders headers = new HttpHeaders();
        logger.info("Target File Name :- " + targetFilename);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + targetFilename);
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        return ResponseEntity.ok().headers(headers).body(convertedFile.toByteArray());
    }

}
