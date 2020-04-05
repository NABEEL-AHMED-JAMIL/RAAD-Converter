package com.raad.converter.aws.imp;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.raad.converter.aws.AwsProperties;
import com.raad.converter.aws.IAwsSESManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class AwsSESManagerImpl implements IAwsSESManager {

    public static final Logger logger = LogManager.getLogger(AwsSESManagerImpl.class);

    private AmazonSimpleEmailService amazonSES;

    public void sendSESEmail(SendEmailRequest request) throws AmazonServiceException, AmazonClientException {
        this.amazonSES.sendEmail(request);
        logger.info("Email Send-Successfully");
    }

    public void sendRawEmail(SendRawEmailRequest request) throws AmazonServiceException, AmazonClientException {
        this.amazonSES.sendRawEmail(request);
        logger.info("Email Send-Successfully");
     }

    @Override
    public AWSCredentials credentials(AwsProperties awsProperties) throws AmazonClientException {
        logger.info("+================AWS-CREDENTIALS-UPDATE-START====================+");
        AWSCredentials credentials = new BasicAWSCredentials(awsProperties.getAccessKey(), awsProperties.getSecretKey());
        logger.info("+================AWS-CREDENTIALS-UPDATE-END====================+");
        return credentials;
    }

    @Override
    public void amazonSES(AwsProperties awsProperties) throws AmazonClientException {
        logger.info("+================AWS-SIMPLE-EMAIL-SERVICE-START====================+");
        this.amazonSES = AmazonSimpleEmailServiceClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials(awsProperties)))
            .withRegion(Regions.fromName(awsProperties.getRegion())).build();
        logger.info("+================AWS-SIMPLE-EMAIL-SERVICE-END====================+");
    }

    @Override
    public void updateAmazonSES(AwsProperties awsProperties) {
        this.amazonSES(awsProperties);
    }

}
