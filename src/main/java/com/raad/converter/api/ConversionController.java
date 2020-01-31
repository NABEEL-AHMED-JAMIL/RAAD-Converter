package com.raad.converter.api;

import com.opencsv.CSVWriter;
import com.raad.converter.convergen.RaadStreamConverter;
import com.raad.converter.convergen.ScraperConstant;
import com.raad.converter.util.FtpFileExchange;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Date;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/conversion")
@Api(tags = {"RAAD-Conversion := RAAD-Conversion EndPoint"})
public class ConversionController {

    public static final String[] HEADER_FILED_BATCH_FILE = new String[] {
            "job_name","url","bookmark","country_name",
            "doc_type","execution_type","keywords_include","keywords_exclude",
            "merge", "lookup_tags","tags_include","tags_exclude","crawling"
    };

    @Autowired
    private FtpFileExchange ftpFileExchange;

    @Autowired
    private RaadStreamConverter raadStreamConverter;

    @RequestMapping(path = "file-converter/v1", method = RequestMethod.POST)
    public ResponseEntity<?> convertPdf(@RequestParam("file") final MultipartFile multipart) throws Exception {
        log.info("File Content Type :- " + multipart.getContentType());
        // will think to take the file in dir or not delete
        ByteArrayOutputStream convertedFile = this.raadStreamConverter.doConvert(multipart.getInputStream(), multipart.getOriginalFilename(), UUID.randomUUID() + ScraperConstant.PDF_EXTENSION);
        this.ftpFileExchange.uploadFile(new ByteArrayInputStream(convertedFile.toByteArray()), UUID.randomUUID()+ ScraperConstant.PDF_EXTENSION);
        final HttpHeaders headers = new HttpHeaders();
        String fileName = FilenameUtils.getBaseName(multipart.getOriginalFilename());
        String targetFilename = String.format("%s.%s", fileName, ScraperConstant.PDF);
        log.info("Target File Name :- " + targetFilename);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + targetFilename);
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        return ResponseEntity.ok().headers(headers).body(convertedFile.toByteArray());
    }

    //@RequestMapping(value = "/downloadBatchFile", method = RequestMethod.GET)
    public ResponseEntity<?> downloadBatchFile() throws IOException {
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer);
        csvWriter.writeNext(HEADER_FILED_BATCH_FILE);
        if(csvWriter != null) { csvWriter.close(); }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Raad-Master-Data "+new Date()+".csv");
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        return new ResponseEntity(IOUtils.toByteArray(new ByteArrayInputStream(writer.toString().getBytes())), headers, HttpStatus.OK );
    }

    public static void main(String args[]) {
        File file = new File("C:\\Users\\Nabeel.Ahmed\\Downloads\\hwp file");
        StringBuilder builder = new StringBuilder();
        for(String fileName: file.list()) {
            builder.append(String.format("<p>%s <a href=\"%s\">%s</a></p>\n", fileName,fileName,fileName));
        }
        System.out.println(builder);
    }

}
