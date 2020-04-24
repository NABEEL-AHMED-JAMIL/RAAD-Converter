package com.raad.converter.worker;

import com.amazonaws.services.s3.model.S3Object;
import com.raad.converter.aws.AwsProperties;
import com.raad.converter.aws.imp.AwsBucketManagerImpl;
import com.raad.converter.domain.FileDetail;
import com.raad.converter.domain.FileSocket;
import com.raad.converter.model.beans.FileInfo;
import com.raad.converter.model.service.FileInfoDetailService;
import com.raad.converter.util.ExceptionUtil;
import com.raad.converter.util.SocketServerComponent;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@Scope(value="prototype")
public class FileDownloadByRunnableWorker implements Runnable {

    public Logger logger = LoggerFactory.getLogger(FileDownloadByRunnableWorker.class);

    private int priority;
    private String workerName;
    private List<FileInfo> fileInfos;
    private String token;

    private static String region = "ap-1";
    private static String accessKey = "A-F";
    private static String secretKey = "I-b";
    private static String bucketName = "r-t";

    @Autowired
    private SocketServerComponent socketServerComponent;
    @Autowired
    private FileInfoDetailService fileInfoDetailService;
    @Autowired
    private AwsBucketManagerImpl awsBucketManager;


    public FileDownloadByRunnableWorker() { }

    public void fileDownload(FileDetail fileDetail) {
        try {
            logger.info("Finding File Detail With Ids");
            this.token = fileDetail.getToke();
            this.fileInfos = this.fileInfoDetailService.findByFileInfoIdIn(fileDetail.getIds());
            logger.info("File Detail With Ids are {} ", fileInfos.size());
        } catch (Exception ex) {
            logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
        }
    }

    @Override
    public void run() {
        try {
            if(fileInfos != null && !fileInfos.isEmpty()) {
                // start the process
                this.awsBucketManager.updateAmazonBucket(new AwsProperties(region, accessKey, secretKey));
                logger.info("Login the aws account for access the s3");
                for(FileInfo fileInfo:fileInfos) {
                    FileSocket fileSocket = new FileSocket();
                    try {
                        String finalPath = fileInfo.getS3path().replaceAll("//", "/")+"/"+fileInfo.getFileName();
                        S3Object s3Object = this.awsBucketManager.getSource(bucketName, finalPath);
                        InputStream inputStream = s3Object.getObjectContent();
                        logger.info("File Download {} ", fileInfo);
                        fileSocket.setStatus(200);
                        fileSocket.setMessage("File Download Successfully");
                        fileSocket.setFileName(fileInfo.getFileName());
                        fileSocket.setDownload(IOUtils.toByteArray(inputStream));
                        this.socketServerComponent.sendSocketEventToClient(this.token, fileSocket);
                        if(inputStream != null) { inputStream.close(); }
                        if(s3Object != null) { s3Object.close(); }
                    } catch (Exception ex) {
                        logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
                        fileSocket.setStatus(400);
                        fileSocket.setMessage("File Download Fail");
                        fileSocket.setFileName(fileInfo.getFileName());
                        this.socketServerComponent.sendSocketEventToClient(this.token, fileSocket);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Exception :- " + ExceptionUtil.getRootCauseMessage(ex));
        }
    }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }

//    public static void main(String args[]) throws Exception {
//        AwsBucketManagerImpl awsBucketManager = new AwsBucketManagerImpl();
//        awsBucketManager.updateAmazonBucket(new AwsProperties(region, accessKey, secretKey));
//        String finalPath = "raw/WebScraping/dev/Clinical Trials/Asia Pacific/India/2461/2020-03-20/"+"_32020_t9_book_20200320_1.pdf";
//        System.out.println(finalPath);
//        S3Object s3Object = awsBucketManager.getSource(bucketName, finalPath);
//        InputStream inputStream = s3Object.getObjectContent();
//        if(inputStream != null) { inputStream.close(); }
//        if(s3Object != null) { s3Object.close(); }
//        System.out.println("Process Complete");
//    }


}
