package com.raad.converter.api;

import com.raad.converter.convergen.RaadStreamConverter;
import com.raad.converter.convergen.ScraperConstant;
import com.raad.converter.convergen.xml.XmlOutTagInfo;
import com.raad.converter.domain.dto.ResponseDTO;
import com.raad.converter.domain.dto.XmlMakerRequest;
import com.raad.converter.domain.dto.XmlRequest;
import com.raad.converter.util.ExceptionUtil;
import com.raad.converter.util.LocalFileHandler;
import com.raad.converter.util.SocketServerComponent;
import com.raad.converter.util.Util;
import com.raad.converter.util.ScreenShoot;
import io.swagger.annotations.Api;
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
import java.io.*;


@RestController
@RequestMapping("/conversion")
@CrossOrigin(origins = "*")
@Api(tags = {"RAAD-Conversion := RAAD-Conversion EndPoint"})
public class RaadConversionController {

    public Logger logger = LoggerFactory.getLogger(RaadConversionController.class);

    private String PDF_STORE = "pdf";

    @Autowired
    private XmlOutTagInfo outTagInfo;
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

    @RequestMapping(path = "file-converter/v2", method = RequestMethod.POST)
    public ResponseEntity<?> fileConvertV2(@RequestParam("file") final MultipartFile multipart) throws Exception {
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

    @RequestMapping(path = "isValidXmlOrUrl", method = RequestMethod.POST)
    // note d'nt add requested annotation in this request
    public ResponseEntity<?> isValidXmlOrUrl(XmlRequest xmlRequest) {
        try {
            //&& xmlRequest.getFile() != null
            if(Util.urlValidator(xmlRequest.getUrl())) {
                return ResponseEntity.ok().body(new ResponseDTO("Success Process", this.outTagInfo.xmlTagResponseDot(xmlRequest)));
            } else {
                return ResponseEntity.badRequest().body(new ResponseDTO("Request Detail Should No Be Null", null));
            }
        } catch (Exception ex) {
            logger.error("isValidXmlOrUrl -- Error occurred " + ex);
            return ResponseEntity.badRequest().body(new ResponseDTO(ExceptionUtil.getRootCauseMessage(ex), null));
        }
    }

    @RequestMapping(path = "xmlCreateChecker",  method = RequestMethod.POST)
    // note d'nt add requested annotation in this request
    public ResponseEntity<?> isValidXmlCreate(@RequestBody XmlMakerRequest xmlMakerRequest) {
        try {
            if(Util.urlValidator(xmlMakerRequest.getUrl()) && xmlMakerRequest.getTags() != null) {
                return ResponseEntity.ok().body(new ResponseDTO( "Success Process", this.outTagInfo.makeXml(xmlMakerRequest)));
            } else {
                return ResponseEntity.badRequest().body(new ResponseDTO("Request Detail Should No Be Null", "Wrong Input"));
            }
        } catch (Exception ex) {
            logger.error("isValidXmlOrUrl -- Error occurred " + ex);
            return ResponseEntity.badRequest().body(new ResponseDTO(ExceptionUtil.getRootCauseMessage(ex), null));
        }
    }

    @RequestMapping(path = "isValidSchema", method = RequestMethod.POST)
    public ResponseEntity<?> isValidSchema(XmlRequest xmlRequest) {
        File xmlFile = null;
        File schemaFile = null;
        try {
            if(xmlRequest.getXmlFile() != null && xmlRequest.getSchemaFile() != null) {
                xmlFile = this.outTagInfo.convertMultiPartToFile(xmlRequest.getSchemaFile());
                schemaFile = this.outTagInfo.convertMultiPartToFile(xmlRequest.getXmlFile());
                boolean response = this.outTagInfo.validateXml(this.outTagInfo.loadSchemaFromFile(xmlFile), this.outTagInfo.parseXmlDOMByFile(schemaFile));
                return ResponseEntity.ok().body(new ResponseDTO("Xml Valid :- " + response, null));
            } else {
                return ResponseEntity.badRequest().body(new ResponseDTO("Request Detail Should No Be Null", null));
            }
        } catch (Exception ex) {
            logger.error("isValidSchema -- Error occurred " + ex);
            return ResponseEntity.badRequest().body(new ResponseDTO(ExceptionUtil.getRootCauseMessage(ex), null));
        } finally {
            if(xmlFile != null) { xmlFile.delete(); }
            if(schemaFile != null) { schemaFile.delete(); }
        }
    }

}
