package com.raad.converter.api;

import com.raad.converter.domain.FileDetail;
import com.raad.converter.domain.FileInfoDto;
import com.raad.converter.domain.ResponseDTO;
import com.raad.converter.model.service.FileInfoDetailService;
import com.raad.converter.util.ExceptionUtil;
import com.raad.converter.worker.AsyncDALTaskExecutor;
import com.raad.converter.worker.FileDownloadByRunnableWorker;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/socket")
@CrossOrigin(origins = "*")
@Api(tags = {"RAAD-Socket := RAAD-Socket EndPoint"})
public class RaadSocketApi {

    public Logger logger = LoggerFactory.getLogger(RaadSocketApi.class);

    private Random random = new Random();
    @Autowired
    private FileDownloadByRunnableWorker fileDownloadWorker;
    @Autowired
    private AsyncDALTaskExecutor asyncDALTaskExecutor;
    @Autowired
    private FileInfoDetailService fileInfoDetailService;


    @RequestMapping(path = "/save", method = RequestMethod.POST)
    public ResponseEntity<?> save(@RequestBody List<FileInfoDto> fileInfoDtos) {
        try {
            return ResponseEntity.ok().body(new ResponseDTO("Data Save", this.fileInfoDetailService.save(fileInfoDtos)));
        } catch (Exception ex) {
            logger.error("/file/save " + ExceptionUtil.getRootCauseMessage(ex));
            return ResponseEntity.badRequest().body(new ResponseDTO(ExceptionUtil.getRootCauseMessage(ex), null));
        }
    }

    @RequestMapping(path = "/fetchDetail", method = RequestMethod.POST)
    public ResponseEntity<?> fetchDetail(@RequestParam("page") int page, @RequestParam("size") int size) {
        try {
            return ResponseEntity.ok().body(new ResponseDTO("Data Fetch", this.fileInfoDetailService.fetchDetail(page, size)));
        } catch (Exception ex) {
            logger.error("/get/file_detail " + ExceptionUtil.getRootCauseMessage(ex));
            return ResponseEntity.badRequest().body(new ResponseDTO(ExceptionUtil.getRootCauseMessage(ex), null));
        }
    }

    @RequestMapping(path = "/file/download", method = RequestMethod.POST)
    public ResponseEntity<?> fileDownload(@RequestBody FileDetail fileDetail) {
        try {
            int priority = this.random.nextInt(100) + 1;
            this.fileDownloadWorker.setPriority(priority);
            this.fileDownloadWorker.setWorkerName("FileDownloadByRunnableWorker-"+priority);
            this.fileDownloadWorker.fileDownload(fileDetail);
            this.asyncDALTaskExecutor.addTask(this.fileDownloadWorker);
            return ResponseEntity.badRequest().body(new ResponseDTO("Your Process Start", fileDetail));
        } catch (Exception ex) {
            logger.error("/file/download " + ExceptionUtil.getRootCauseMessage(ex));
            return ResponseEntity.badRequest().body(new ResponseDTO(ExceptionUtil.getRootCauseMessage(ex), null));
        }
    }

}
