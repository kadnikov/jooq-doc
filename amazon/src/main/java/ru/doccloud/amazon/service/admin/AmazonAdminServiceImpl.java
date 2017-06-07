package ru.doccloud.amazon.service.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.doccloud.amazon.repository.AmazonRepository;

/**
 *see http://docs.aws.amazon.com/AmazonS3/latest/dev/create-bucket-get-location-example.html#create-bucket-get-location-java
 * for details
 */
@Component("amazonAdminService")
public class AmazonAdminServiceImpl implements AmazonAdminService {


    private final AmazonRepository amazonRepository;

    @Autowired
    public AmazonAdminServiceImpl(AmazonRepository amazonRepository) {
        this.amazonRepository = amazonRepository;
    }

    @Override
    public String createBucket(String bucketName) throws Exception {
       return amazonRepository.createBucket(bucketName);
    }

    @Override
    public void deleteBucket(String bucketName) {
        amazonRepository.deleteBucket(bucketName);
    }
}
