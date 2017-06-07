package ru.doccloud.document.amazon;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * Created by ilya on 6/6/17.
 */
class AmazonConfig {
    BasicAWSCredentials basicAWSCredentials() {
                String awsAccessKeyId = "accessKey1";
        String awsSecretAccessKey = "verySecretKey1";
//    String awsAccessKeyId = environment.getProperty("amazon.aws_access_key_id");
//    String awsSecretAccessKey = environment
//        .getProperty("amazon.aws_secret_access_key");
        return new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
    }

    AmazonS3 amazonS3(BasicAWSCredentials basicAWSCredentials) {
//    String awsRegion = environment.getProperty("amazon.aws_region");
        AmazonS3 amazonS3 = new AmazonS3Client(basicAWSCredentials);
//        amazonS3.setRegion(Region.getRegion(Regions.fromName(awsRegion)));
        amazonS3.setEndpoint("http://doccloud.ru:8000");
        return amazonS3;
    }
}
