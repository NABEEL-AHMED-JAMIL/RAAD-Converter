package com.raad.converter.api;

import com.raad.converter.convergen.xml.XmlOutTagInfo;
import com.raad.converter.domain.ResponseDTO;
import com.raad.converter.domain.XmlMakerRequest;
import com.raad.converter.domain.XmlRequest;
import com.raad.converter.util.ExceptionUtil;
import com.raad.converter.util.Util;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/conversion")
@CrossOrigin(origins = "*")
@Api(tags = {"RAAD-Xml := RAAD-XML EndPoint"})
public class RaadXmlApi {

    public Logger logger = LoggerFactory.getLogger(RaadXmlApi.class);

    @Autowired
    private XmlOutTagInfo outTagInfo;

    @RequestMapping(path = "isValidXmlOrUrl", method = RequestMethod.POST)
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
