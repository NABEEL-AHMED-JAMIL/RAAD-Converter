package com.raad.converter.api;

import com.raad.converter.convergen.RaadStreamConverter;
import com.raad.converter.convergen.ScraperConstant;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/conversion")
@Api(tags = {"RAAD-Conversion := RAAD-Conversion EndPoint"})
public class ConversionController {

    @Autowired
    private RaadStreamConverter raadStreamConverter;

    @RequestMapping(path = "file-converter/v1", method = RequestMethod.POST)
    public ResponseEntity<?> convertPdf(@RequestParam("file") final MultipartFile multipart) throws Exception {
        log.info("File Content Type :- " + multipart.getContentType());
        // will think to take the file in dir or not delete
        ByteArrayOutputStream convertedFile = this.raadStreamConverter.doConvert(multipart.getInputStream(),
                multipart.getOriginalFilename(), UUID.randomUUID() + ScraperConstant.PDF_EXTENSION);
        final HttpHeaders headers = new HttpHeaders();
        String fileName = FilenameUtils.getBaseName(multipart.getOriginalFilename());
        String targetFilename = String.format("%s.%s", fileName, ScraperConstant.PDF);
        log.info("Target File Name :- " + targetFilename);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + targetFilename);
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        return ResponseEntity.ok().headers(headers).body(convertedFile.toByteArray());
    }

}
