package ru.doccloud.amazon.repository;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.doccloud.common.util.JsonNodeParser;
import ru.doccloud.storage.storagesettings.StorageAreaSettings;

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
    private static final String AMZON_ENDPOINT_PARAM = "awsEndpoint";
    private static final String AMAZON_ACCESS_KEY_PARAM = "awsAccessKey";
    private static final String AMAZON_ACCESS_SECRET_KEY_PARAM = "awsSecretAccessKey";
    private static final String AMAZON_BUCKET_NAME_PARAM = "bucketName";

    private final AmazonS3 amazonS3;

    private JsonNode settingsNode;

    @Autowired
    public AmazonReposiroryImpl(StorageAreaSettings storageAreaSettings) throws Exception {
        this.settingsNode = (JsonNode) storageAreaSettings.getStorageSetting();
        this.amazonS3 = amazonS3(basicAWSCredentials());
    }

    @Override
    public String uploadFile(String bucketName, UUID uuid, byte[] fileArr) throws Exception {
        LOGGER.trace("entering uploadFile(bucketName={}, uuid={}, byte.lenght={})", bucketName, uuid, fileArr.length);

        final String mime = "application/octet-stream";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileArr);
        final String key = uploadFile(bucketName, inputStream, (long) inputStream.available(), mime, uuid);

        LOGGER.trace("leaving uploadFile(): objectKey {}", key);
        return key;
    }

    @Override
    public byte[] readFile(String key) throws Exception {
        LOGGER.trace("entering readFile(key={})", key);
        final String bucketName = getBucketName();

        LOGGER.trace("readFile(): bucketName", bucketName);
        final S3Object s3Object = amazonS3.getObject(bucketName, key);

        byte[] fileArr = com.amazonaws.util.IOUtils.toByteArray(s3Object.getObjectContent());

        LOGGER.trace("leaving readFile()", fileArr != null ? fileArr.length : null);
        return fileArr;
    }

    @Override
    public String createBucket(String bucketName) throws Exception {
        LOGGER.trace("entering createBucket(bucketName={})", bucketName);
        Bucket bucket  = amazonS3.createBucket(bucketName);
        LOGGER.trace("leaving createBucket(): bucketName {}", bucket.getName());
        return bucket.getName();
    }

    @Override
    public void deleteBucket(String bucketName) throws Exception {
        LOGGER.trace("entering deleteBucket(bucketName={})", bucketName);
        amazonS3.deleteBucket(bucketName);
        LOGGER.trace("leaving deleteBucket(): bucket was deleted");
    }

    private String getBucketName() throws Exception {
        final String bucketName = JsonNodeParser.getValueJsonNode(settingsNode, AMAZON_BUCKET_NAME_PARAM);
        if(StringUtils.isBlank(bucketName))
            throw new Exception("Bucket name was not found in settings");

        return bucketName;
    }


    private String uploadFile(String bucketName, InputStream inputStream, Long contentLength, String mimeType, UUID uuid) {

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        metadata.setContentType(mimeType);

        String objectPathKey = formatObjectPathKey(uuid);

        amazonS3.putObject(
                new PutObjectRequest(bucketName, objectPathKey, inputStream, metadata));

        return objectPathKey;
    }

    private String formatObjectPathKey(UUID uuid) {
        return uuid.toString();
    }

    private BasicAWSCredentials basicAWSCredentials() throws Exception {

        final String awsAccessKeyId = JsonNodeParser.getValueJsonNode(settingsNode, AMAZON_ACCESS_KEY_PARAM);
        final String awsSecretAccessKey =  JsonNodeParser.getValueJsonNode(settingsNode, AMAZON_ACCESS_SECRET_KEY_PARAM);
        return new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
    }

    private AmazonS3 amazonS3(BasicAWSCredentials basicAWSCredentials) throws Exception {
        AmazonS3 amazonS3 = new AmazonS3Client(basicAWSCredentials);
        amazonS3.setEndpoint(JsonNodeParser.getValueJsonNode(settingsNode, AMZON_ENDPOINT_PARAM));
        amazonS3.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).build());
        return amazonS3;
    }
}

