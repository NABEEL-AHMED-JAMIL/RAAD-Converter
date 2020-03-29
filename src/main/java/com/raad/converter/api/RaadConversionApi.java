package com.raad.converter.api;

import com.raad.converter.convergen.RaadStreamConverter;
import com.raad.converter.convergen.ScraperConstant;
import com.raad.converter.domain.FilePath;
import com.raad.converter.domain.FileWithObject;
import com.raad.converter.util.ExceptionUtil;
import com.raad.converter.util.LocalFileHandler;
import com.raad.converter.util.SocketServerComponent;
import com.raad.converter.util.ScreenShoot;
import io.swagger.annotations.Api;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/conversion")
@CrossOrigin(origins = "*")
@Api(tags = {"RAAD-Conversion := RAAD-Conversion EndPoint"})
public class RaadConversionApi {

    public Logger logger = LoggerFactory.getLogger(RaadConversionApi.class);

    private String PDF_STORE = "pdf";

    @Autowired
    private RaadStreamConverter raadStreamConverter;
    @Autowired
    private SocketServerComponent socketServerComponent;
    @Autowired
    private LocalFileHandler localFileHandler;
    @Autowired
    private ScreenShoot screenShoot;

    @PostConstruct
    public void init() {
        localFileHandler.makeDir(File.separator+PDF_STORE);
    }

    @RequestMapping(path = "file-converter/v1", method = RequestMethod.POST)
    public ResponseEntity<?> converterV1(@RequestParam("file") final MultipartFile multipart) throws Exception {
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

    @RequestMapping(path = "file-converter/v2", method = RequestMethod.POST)
    public ResponseEntity<?> converterV2(@RequestParam("file") final MultipartFile multipart) throws Exception {
        try {
            this.socketServerComponent.sendSocketEventToClient("==============File Convert Start==============");
            logger.info("File Content Type :- " + multipart.getContentType());
            this.socketServerComponent.sendSocketEventToClient("File Content Type :- " + multipart.getContentType());
            String fileName = FilenameUtils.getBaseName(multipart.getOriginalFilename());
            this.socketServerComponent.sendSocketEventToClient("Original File Name :- " + fileName);
            String targetFilename = String.format("%s%s", fileName, ScraperConstant.PDF_EXTENSION);
            this.socketServerComponent.sendSocketEventToClient("Target File Name :- " + targetFilename);
            // will think to take the file in dir or not delete
            ByteArrayOutputStream convertedFile = this.raadStreamConverter.doConvert(multipart.getInputStream(), multipart.getOriginalFilename(), targetFilename);
            this.socketServerComponent.sendSocketEventToClient("File Converter Successfully.");
            // after convert successfully store the pdf file
            this.localFileHandler.saveFile(convertedFile, File.separator+PDF_STORE+File.separator+targetFilename);
            final HttpHeaders headers = new HttpHeaders();
            logger.info("Target File Name :- " + targetFilename);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + targetFilename);
            headers.setContentType(MediaType.parseMediaType("application/pdf"));
            this.socketServerComponent.sendSocketEventToClient("==============File Convert End==============");
            return ResponseEntity.ok().headers(headers).body(convertedFile.toByteArray());
        } catch (Exception ex) {
            this.socketServerComponent.sendSocketEventToClient("Error :- " + ExceptionUtil.getRootCauseMessage(ex));
            throw ex;
        }
    }

    @RequestMapping(path = "file-converter/v3", method = RequestMethod.POST)
    public ResponseEntity<?> converterV3(@RequestParam(name = "op_file_name", required = true) String op_file_name,
        @RequestBody @Valid List<FilePath> filePaths) {
        try {
            List<ByteArrayOutputStream> baOutS = new ArrayList<>();
            Iterator<FilePath> filePathIterator = filePaths.iterator();
            ByteArrayOutputStream baOut;
            while (filePathIterator.hasNext()) {
                FilePath filePath = filePathIterator.next();
                InputStream inputStream = getFileStream(filePath.getPath());
                String mockFileName = UUID.randomUUID().toString()+ ScraperConstant.PDF_EXTENSION;
                baOut = this.raadStreamConverter.doConvert(inputStream, filePath.getPath(), mockFileName);
                if(baOut != null && baOut.size() > 0) { baOutS.add(baOut); }
                // close the stream
                if(inputStream != null) { inputStream.close(); }
            }
            // if all oky marege the file into file output file
            if(baOutS.size() > 1) {
                baOut = new ByteArrayOutputStream();
                margeFile(baOutS.stream().map(byteOutputStream -> {
                    return new ByteArrayInputStream(byteOutputStream.toByteArray());
                }).collect(Collectors.toList()), baOut);
            } else {
                baOut = baOutS.get(0);
            }
            final HttpHeaders headers = new HttpHeaders();
            String targetFilename = String.format("%s.%s", op_file_name, ScraperConstant.PDF);
            logger.info("Target File Name :- " + targetFilename);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + targetFilename);
            headers.setContentType(MediaType.parseMediaType("application/pdf"));
            return ResponseEntity.ok().headers(headers).body(baOut.toByteArray());
        } catch (Exception ex) {
            String exMessage = ex.getMessage();
            String errorJson = "{ \"error\" : \"%s\"}";
            return ResponseEntity.ok().body(String.format(errorJson, exMessage));
        }
    }


    @RequestMapping(path = "file-converter/v4", method = RequestMethod.POST)
    public ResponseEntity<?> converterV4(FileWithObject object) {
        try {
            List<ByteArrayOutputStream> baOutS = new ArrayList<>();
            ByteArrayOutputStream baOut;
            for(MultipartFile multipartFile: object.getFiles()) {
                String mockFileName = UUID.randomUUID().toString()+ ScraperConstant.PDF_EXTENSION;
                baOut = this.raadStreamConverter.doConvert(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), mockFileName);
                if(baOut != null && baOut.size() > 0) { baOutS.add(baOut); }
            }
            if(baOutS.size() > 1) {
                baOut = new ByteArrayOutputStream();
                margeFile(baOutS.stream().map(byteOutputStream -> {
                    return new ByteArrayInputStream(byteOutputStream.toByteArray());
                }).collect(Collectors.toList()), baOut);
            } else {
                baOut = baOutS.get(0);
            }
            final HttpHeaders headers = new HttpHeaders();
            String targetFilename = String.format("%s.%s", object.getOp_file_name(), ScraperConstant.PDF);
            logger.info("Target File Name :- " + targetFilename);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + targetFilename);
            headers.setContentType(MediaType.parseMediaType("application/pdf"));
            return ResponseEntity.ok().headers(headers).body(baOut.toByteArray());
        } catch (Exception ex) {
            String exMessage = ex.getMessage();
            String errorJson = "{ \"error\" : \"%s\"}";
            return ResponseEntity.ok().body(String.format(errorJson, exMessage));
        }
    }

    private InputStream getFileStream(String sourceFile) throws Exception {
        return new BufferedInputStream(new FileInputStream(sourceFile));
    }

    private void margeFile(List<InputStream> inputStreams, ByteArrayOutputStream outputStream) throws IOException {
        PDFMergerUtility ut = new PDFMergerUtility();
        ut.addSources(inputStreams);
        ut.setDestinationStream(outputStream);
        ut.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
    }

}
