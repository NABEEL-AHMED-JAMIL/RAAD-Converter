package com.raad.converter.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;

public interface IAws {

    public AWSCredentials credentials(AwsProperties awsProperties) throws AmazonClientException;
}
