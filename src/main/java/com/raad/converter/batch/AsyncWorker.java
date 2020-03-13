package com.raad.converter.batch;

import com.raad.converter.convergen.RaadStreamConverter;
import com.raad.converter.convergen.ScraperConstant;
import com.raad.converter.util.LocalFileHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Component
@Scope("prototype")
public class AsyncWorker implements Runnable {

    public Logger logger = LogManager.getLogger(AsyncWorker.class);

    private String filePath;
    private int fileNumber;

    private String PDF_STORE = "pdf";
    private String TEXT_STORE = "txt";
    @Autowired
    public RaadStreamConverter raadStreamConverter;
    @Autowired
    private LocalFileHandler localFileHandler;

    public AsyncWorker() { }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public int getFileNumber() { return fileNumber; }
    public void setFileNumber(int fileNumber) { this.fileNumber = fileNumber; }

    @Override
    public void run() {
        logger.info("===>>>>>>File Converter-Save-Start<<<<<<=======");
        try {
            File initialFile = new File(getFilePath());
            InputStream targetStream = new FileInputStream(initialFile);
            String fileName = FilenameUtils.getBaseName(initialFile.getAbsolutePath());
            String targetFilename = String.format("%s%s", fileName, ScraperConstant.PDF_EXTENSION);
            ByteArrayOutputStream convertedFile = this.raadStreamConverter.doConvert(targetStream, initialFile.getName(), targetFilename);
            this.localFileHandler.saveFile(convertedFile, File.separator+PDF_STORE+File.separator+getFileNumber()+targetFilename);
        } catch (Exception ex) {
            logger.error("Error :- " + ex);
        }
        logger.info("===>>>>>>File Converter-Save-End<<<<<<=======");
    }

}
