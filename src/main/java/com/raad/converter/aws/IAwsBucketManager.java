package com.raad.converter.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IAwsBucketManager extends IAws {

    public String createBucket(final String bucketName) throws AmazonServiceException;

    public boolean isBucketExist(final String bucketName) throws AmazonClientException;

    public Set<String> listBuckets() throws AmazonClientException;

    public boolean deleteBucket(String bucketName) throws AmazonClientException;

    public boolean isObjKeyExist(String bucketName, String objectKey) throws AmazonClientException;

    public Map<String, Object> uploadToBucket(final File file, final String bucketName) throws AmazonClientException;

    public List<String> listByFullPathPrefix(final String bucket, final String s3prefix) throws AmazonClientException;

    public boolean deleteBucketObject(final String objKey, final String bucketName) throws AmazonClientException;

    public ObjectListing getListing(String bucketName, String prefix) throws SdkClientException, AmazonClientException;

    public void listBucketObjects(String bucketName) throws AmazonClientException;

    public S3Object getSource(String bucketName, String fileName);

    public Map<String, Object> getObjectMetadata(final String objKey, final String bucketName) throws AmazonClientException;

    public void amazonS3(AwsProperties awsProperties) throws AmazonClientException;

    public void amazonS3Default(AwsProperties awsProperties) throws AmazonClientException;

    public void updateAmazonBucket(AwsProperties awsProperties);


}
