package com.raad.converter.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

public interface IAwsSESManager extends IAws {

    public void sendSESEmail(SendEmailRequest request) throws AmazonServiceException, AmazonClientException;

    public void sendRawEmail(SendRawEmailRequest request) throws AmazonServiceException, AmazonClientException;

    public void amazonSES(AwsProperties awsProperties) throws AmazonClientException;

    public void updateAmazonSES(AwsProperties awsProperties);
}
