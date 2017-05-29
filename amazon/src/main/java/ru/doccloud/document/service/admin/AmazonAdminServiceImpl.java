package ru.doccloud.document.service.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.doccloud.document.repository.AmazonRepository;

/**
 *see http://docs.aws.amazon.com/AmazonS3/latest/dev/create-bucket-get-location-example.html#create-bucket-get-location-java
 * for details
 */
@Service
public class AmazonAdminServiceImpl implements AmazonAdminService {


    private final AmazonRepository amazonRepository;

    @Autowired
    public AmazonAdminServiceImpl(AmazonRepository amazonRepository) {
        this.amazonRepository = amazonRepository;
    }


    @Override
    public String createBucket(String bucketName) {
       return amazonRepository.createBucket(bucketName);
    }

    @Override
    public void deleteBucket(String bucketName) {
        amazonRepository.deleteBucket(bucketName);
    }
}
