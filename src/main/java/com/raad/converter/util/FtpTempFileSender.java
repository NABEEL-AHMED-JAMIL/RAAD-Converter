package com.raad.converter.util;

import com.raad.converter.convergen.ScraperConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.UUID;


@Slf4j
@Component
@Scope(value="prototype")
public class FtpTempFileSender {

    @Autowired
    private FtpFileExchange ftpFileExchange;

    public FtpTempFileSender() {}

    public void sendFileToSend() {
        try {
            //this.ftpFileExchange.setDirectoryPath(String.format("/docker/%s", UUID.randomUUID()));
            for(int i=0; i<10; i++) {
                final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                this.getMockUpdate(byteArrayOutputStream);
                this.ftpFileExchange.uploadFile(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()),
                        UUID.randomUUID()+ ScraperConstant.TXT_EXTENSION);
            }
        } catch (Exception ex) {
            log.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
        }
    }

    private void getMockUpdate(ByteArrayOutputStream byteArrayOutputStream) throws Exception {
        for (int i=0; i<1000; i++) {
            String device = UUID.randomUUID().toString();
            log.info("Device With Thread :- " + device);
            byteArrayOutputStream.write(device.getBytes());
        }
    }
}
