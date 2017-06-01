package ru.doccloud.amazon.repository;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.activation.MimetypesFileTypeMap;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 *  see http://docs.aws.amazon.com/AmazonS3/latest/dev/RetrievingObjectUsingJava.html.
 * for details
 */
@Component("amazonRepository")
public class AmazonReposiroryImpl implements AmazonRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonReposiroryImpl.class);


    private AmazonS3 amazonS3;

//    todo add autowired after keys generation
//    public AmazonReposiroryImpl() {
////        this.amazonS3 = amazonS3(basicAWSCredentials());
//        this.amazonS3 = null;
//    }


    private String formatObjectPathKey(String objectKey, UUID uuid) {
        return uuid + "/" + objectKey;
    }

    @Override
    public String uploadFile(String rootName, UUID uuid, byte[] fileArr) {
        LOGGER.trace("entering uploadFile(rootNAme={}, uuid={}, byte.lenght={})", rootName, uuid, fileArr.length);

        final String bucketName = getBucketName();
        final MimetypesFileTypeMap map = new MimetypesFileTypeMap();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileArr.length);

        PutObjectResult putObjectResult = getAmazonS3().putObject(bucketName, formatObjectPathKey(rootName, uuid), new ByteArrayInputStream(fileArr), metadata);
        LOGGER.trace("leaving uploadFile(): putObjectResult={}", putObjectResult);
        return putObjectResult.getETag();
    }

    @Override
    public byte[] readFile(String key) throws Exception {
        LOGGER.trace("entering readFile(key={})", key);
        String bucketName = getBucketName();
        LOGGER.trace("readFile(): bucketName={}", bucketName);
                InputStream objectData = null;
        try {
            GetObjectRequest rangeObjectRequest = new GetObjectRequest(
                    bucketName, key);
            rangeObjectRequest.setRange(0, 10); // retrieve 1st 11 bytes.
            S3Object objectPortion = getAmazonS3().getObject(rangeObjectRequest);

             objectData = objectPortion.getObjectContent();
            byte[] fileArr =  IOUtils.toByteArray(objectData);

            LOGGER.trace("leaving readFile()", fileArr != null ? fileArr.length : null);
            return fileArr;
        }
        finally {
        // Process the objectData stream.
            if(objectData != null)
                objectData.close();
        }
    }

    @Override
    public String createBucket(String bucketName) {
        Bucket bucket  = getAmazonS3().createBucket(bucketName);
        return bucket.getName();
    }

    @Override
    public void deleteBucket(String bucketName) {
        getAmazonS3().deleteBucket(bucketName);
    }

    private String getBucketName(){
//        todo returns bucketName from database

        return "";
    }

    private BasicAWSCredentials basicAWSCredentials() {

        String awsAccessKeyId = "";
        String awsSecretAccessKey = "";
        return new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
    }

    private AmazonS3 amazonS3(BasicAWSCredentials basicAWSCredentials) {
        String awsRegion = "";
        AmazonS3 amazonS3 = new AmazonS3Client(basicAWSCredentials);
        amazonS3.setRegion(com.amazonaws.regions.Region.getRegion(Regions.fromName(awsRegion)));
        return amazonS3;
    }

    private AmazonS3 getAmazonS3(){
        if (amazonS3 == null)
            return amazonS3(basicAWSCredentials());

        return amazonS3;
    }
}

