package ru.doccloud.document.service;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

/**
 *  see http://docs.aws.amazon.com/AmazonS3/latest/dev/RetrievingObjectUsingJava.html.
 * for details
 */
@Service
public class AmazonServiceImpl implements AmazonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonServiceImpl.class);

    private final AmazonS3 amazonS3;

    @Autowired
    public AmazonServiceImpl(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public String uploadFile(String bucketName, InputStream inputStream, Long contentLength, String mimeType, String key) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        metadata.setContentType(mimeType);

        String objectPathKey = formatObjectPathKey(key);

//        todo it needs to clarify which must be returned objectPathKey or Etag
        return amazonS3.putObject(
                new PutObjectRequest(bucketName, objectPathKey, inputStream, metadata)).getETag();
    }

    @Override
    public void deleteFromAmazon(String bucketName, String objectKey){
        amazonS3.deleteObject(bucketName, objectKey);
    }

    @Override
    public byte[] readFile(String bucketName, String key) throws Exception {

        InputStream objectData = null;
try {
    AmazonS3 s3Client = new AmazonS3Client(new ProfileCredentialsProvider());

    GetObjectRequest rangeObjectRequest = new GetObjectRequest(
            bucketName, key);
    rangeObjectRequest.setRange(0, 10); // retrieve 1st 11 bytes.
    S3Object objectPortion = s3Client.getObject(rangeObjectRequest);

     objectData = objectPortion.getObjectContent();
    return IOUtils.toByteArray(objectData);

}
finally {
// Process the objectData stream.
    if(objectData != null)
        objectData.close();
}



    }

    private String formatObjectPathKey(String objectKey) {
        return UUID.randomUUID() + "/" + objectKey;
    }
}

