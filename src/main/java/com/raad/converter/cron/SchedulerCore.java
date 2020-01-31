package com.raad.converter.cron;


import com.raad.converter.util.ExceptionUtil;
import com.raad.converter.util.FtpTempFileSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope(value="prototype")
public class SchedulerCore {

    @Autowired
    private FtpTempFileSender ftpTempFileSender;

    //@Scheduled(fixedDelay = 5000)
    public void scheduleFixedDelayTask() {
        try {
            this.ftpTempFileSender.sendFileToSend();
        } catch (Exception ex) {
            log.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
        }
    }
}
