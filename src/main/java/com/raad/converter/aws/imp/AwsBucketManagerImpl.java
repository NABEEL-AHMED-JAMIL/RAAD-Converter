package com.raad.converter.aws.imp;

import com.amazonaws.AmazonClientException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.model.NotFoundException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.raad.converter.aws.AwsProperties;
import com.raad.converter.aws.IAwsBucketManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class AwsBucketManagerImpl implements IAwsBucketManager {

    public static final Logger logger = LogManager.getLogger(AwsBucketManagerImpl.class);

    private final String BUCKET_NAME = "bucketName";
    private final String OBJ_KEY = "objKey";
    private final String SIGNED_URL = "signedUrl";

    private AmazonS3 amazonS3;

    @Override
    public String createBucket(String bucketName) throws AmazonClientException {
        String bucketLocation = null;
        if(this.isBucketExist(bucketName)) {
            this.amazonS3.createBucket(new CreateBucketRequest(bucketName));
            bucketLocation = this.amazonS3.getBucketLocation(new GetBucketLocationRequest(bucketName));
            logger.info("New Bucket location:- " + bucketLocation);
        }
        return bucketLocation;
    }

    @Override
    public boolean isBucketExist(String bucketName) throws AmazonClientException {
        if(bucketName != null && !bucketName.equals("")) {
            return this.amazonS3.doesBucketExistV2(bucketName);
        }
        throw new NullPointerException("Invalid bucket name");
    }

    @Override
    public Set<String> listBuckets() throws AmazonClientException {
        Set<String> bucketsDetail = new HashSet<>();
        for (Bucket bucket : this.amazonS3.listBuckets()) {
            logger.info(" -----> " + bucket.getName());
            bucketsDetail.add(bucket.getName());
        }
        return bucketsDetail;
    }

    @Override
    public List<String> listByFullPathPrefix(final String bucket, final String s3prefix) throws AmazonClientException {
        return this.amazonS3.listObjects(bucket, s3prefix)
             .getObjectSummaries().stream()
             .map(S3ObjectSummary::getKey)
             .collect(Collectors.toList());
    }

    @Override
    public boolean deleteBucket(String bucketName) throws AmazonClientException {
        if (this.amazonS3.doesBucketExistV2(bucketName)) {
            this.amazonS3.deleteBucket(bucketName);
            return true;
        }
        throw new NotFoundException("Bucket Not Found Exception");
    }

    @Override
    public boolean isObjKeyExist(String bucketName, String objectKey) throws AmazonClientException {
        if(isBucketExist(bucketName) && (objectKey != null && !objectKey.equals(""))) {
            return this.amazonS3.doesObjectExist(bucketName,objectKey);
        }
        throw new NullPointerException("Invalid objectKey name");
    }

    @Override
    public Map<String, Object> uploadToBucket(File file, String bucketName) throws AmazonClientException {
        String objKey = this.generateFileName(file);
        logger.debug("Uploading a new object to S3 from a file > " + objKey);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objKey, file);
        this.amazonS3.putObject(putObjectRequest);
        /* get signed URL (valid for 2 years day) */
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objKey);
        generatePresignedUrlRequest.setMethod(HttpMethod.GET);
        generatePresignedUrlRequest.setExpiration(DateTime.now().plusYears(2).toDate());
        URL signedUrl = this.amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        // raw data (bucket-name,key,url,size,stock-id)
        HashMap<String, Object> s3Response = new HashMap<>();
        s3Response.put(BUCKET_NAME, bucketName);
        s3Response.put(OBJ_KEY, objKey);
        s3Response.put(SIGNED_URL, signedUrl);
        logger.info("Detail :- " + s3Response);
        return s3Response;
    }

    @Override
    public Map<String, Object> uploadToBucket(InputStream inputStream, String fileName, String bucketName) throws AmazonClientException {
        logger.debug("Uploading a new object to S3 from a file > " + fileName);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream, new ObjectMetadata());
        this.amazonS3.putObject(putObjectRequest);
        /* get signed URL (valid for 2 years day) */
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, fileName);
        generatePresignedUrlRequest.setMethod(HttpMethod.GET);
        generatePresignedUrlRequest.setExpiration(DateTime.now().plusYears(2).toDate());
        URL signedUrl = this.amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        // raw data (bucket-name,key,url,size,stock-id)
        HashMap<String, Object> s3Response = new HashMap<>();
        s3Response.put(BUCKET_NAME, bucketName);
        s3Response.put(OBJ_KEY, fileName);
        s3Response.put(SIGNED_URL, signedUrl);
        logger.info("Detail :- " + s3Response);
        return s3Response;
    }

    @Override
    public boolean deleteBucketObject(String objKey, String bucketName) throws AmazonClientException {
        logger.warn("Deleting an object");
        this.amazonS3.deleteObject(bucketName, objKey);
        return true;
    }

    @Override
    public void listBucketObjects(String bucketName) throws AmazonClientException {
        ObjectListing objectListing = this.amazonS3.
             listObjects(new ListObjectsRequest().withBucketName(bucketName).withPrefix("My "));
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            logger.info(objectSummary);
        }
    }

    @Override
    public S3Object getSource(String bucketName, String fileName) throws AmazonClientException{
        return this.amazonS3.getObject(bucketName, fileName);
    }

    @Override
    public ObjectListing getListing(String bucketName, String prefix) throws AmazonClientException {
        return this.amazonS3.listObjects(bucketName, prefix);
    }

    @Override
    public Map<String, Object> getObjectMetadata(String objKey, String bucketName) throws AmazonClientException {
        return this.amazonS3.getObject(new GetObjectRequest(bucketName, objKey))
             .getObjectMetadata().getRawMetadata();
    }

    @Override
    public AWSCredentials credentials(AwsProperties awsProperties) throws AmazonClientException {
        return new BasicAWSCredentials(awsProperties.getAccessKey(), awsProperties.getSecretKey());
    }

    @Override
    public void amazonS3(AwsProperties awsProperties) throws AmazonClientException {
        logger.debug("+================AWS--START====================+");
        this.amazonS3 = AmazonS3ClientBuilder.standard()
          .withCredentials(new AWSStaticCredentialsProvider(this.credentials(awsProperties)))
          .withRegion(Regions.fromName(awsProperties.getRegion())).build();
        logger.debug("+================AWS-S3-END====================+");
    }

    @Override
    public void amazonS3Default(AwsProperties awsProperties) throws AmazonClientException {
        logger.debug("+================AWS--START====================+");
        this.amazonS3 = AmazonS3ClientBuilder.standard().withRegion(awsProperties.getRegion()).build();
        logger.debug("+================AWS-S3-END====================+");
    }

    @Override
    public void updateAmazonBucket(AwsProperties awsProperties) {
        this.amazonS3(awsProperties);
    }

    private String generateFileName(File file) {
        return UUID.randomUUID() + "-" + new Date().getTime() + "-" + file.getName();
    }
}
