package ru.doccloud.document.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *see http://docs.aws.amazon.com/AmazonS3/latest/dev/create-bucket-get-location-example.html#create-bucket-get-location-java
 * for details
 */
@Service
public class AmazonAdminServiceImpl implements AmazonAdminService {


    private final AmazonS3 amazonS3;

    @Autowired
    public AmazonAdminServiceImpl(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public String createBucket(String bucketName) {
        Bucket bucket  = amazonS3.createBucket(bucketName);
        return bucket.getName();
    }

    @Override
    public void deleteBucket(String bucketName) {
        amazonS3.deleteBucket(bucketName);
    }
}
