package ru.doccloud.document.amazon;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;

/**
 * Created by ilya on 6/6/17.
 */
class AmazonConfig {
    BasicAWSCredentials basicAWSCredentials() {
        String awsAccessKeyId = "accessKey1";
        String awsSecretAccessKey = "verySecretKey1";
        return new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
    }

    AmazonS3 amazonS3(BasicAWSCredentials basicAWSCredentials) {
        AmazonS3 amazonS3 = new AmazonS3Client(basicAWSCredentials);
        amazonS3.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).build());
        amazonS3.setEndpoint("http://doccloud.ru:8000");
        return amazonS3;
    }
}
