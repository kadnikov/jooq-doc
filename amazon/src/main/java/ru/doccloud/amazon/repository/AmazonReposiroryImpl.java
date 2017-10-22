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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *  see http://docs.aws.amazon.com/AmazonS3/latest/dev/RetrievingObjectUsingJava.html.
 * for details
 */
@Component("amazonRepository")
public class AmazonReposiroryImpl implements AmazonRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonReposiroryImpl.class);
    private static final String AMAZON_ENDPOINT_PARAM = "awsEndpoint";
    private static final String AMAZON_ACCESS_KEY_PARAM = "awsAccessKey";
    private static final String AMAZON_ACCESS_SECRET_KEY_PARAM = "awsSecretAccessKey";
    private static final String AMAZON_BUCKET_NAME_PARAM = "bucketName";

    private static final String MIME_TYPE = "application/octet-stream";

    @Autowired
    public AmazonReposiroryImpl() throws Exception {
    }

    @Override
    public String uploadFile(JsonNode storageSettings, UUID uuid, byte[] fileArr) throws Exception {
        LOGGER.trace("entering uploadFile(storageSettings={}, uuid={}, byte.lenght={})", storageSettings, uuid, fileArr.length);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileArr);
        final String key = uploadFile(storageSettings, inputStream, (long) inputStream.available(), MIME_TYPE, uuid);

        LOGGER.trace("leaving uploadFile(): objectKey {}", key);
        return key;
    }

    @Override
    public byte[] readFile(JsonNode storageSettings, String key) throws Exception {
        LOGGER.trace("entering readFile(storageSettings= {}, key={})", storageSettings, key);

        final S3Object s3Object = getAmazonS3(storageSettings).getObject(getBucketName(storageSettings), key);

        byte[] fileArr = com.amazonaws.util.IOUtils.toByteArray(s3Object.getObjectContent());

        LOGGER.trace("leaving readFile()", fileArr != null ? fileArr.length : null);
        return fileArr;
    }

    @Override
    public String createBucket(JsonNode storageSettings) throws Exception {
        AmazonS3 amazonS3 = getAmazonS3(storageSettings);
        return createBucketWithName(amazonS3, getBucketName(storageSettings));
    }

    @Override
    public void deleteBucket(JsonNode storageSettings) throws Exception {
        LOGGER.trace("entering deleteBucket(storageSettings={})", storageSettings);

        getAmazonS3(storageSettings).deleteBucket(getBucketName(storageSettings));
        LOGGER.trace("leaving deleteBucket(): bucket was deleted");
    }

    private String createBucketWithName(AmazonS3 amazonS3, String bucketName) throws Exception {
        LOGGER.trace("entering createBucket(bucketName={})", bucketName);
        final Bucket bucket  = amazonS3.createBucket(bucketName);
        LOGGER.trace("leaving createBucket(): bucketName {}", bucket.getName());
        return bucket.getName();
    }

    private String getBucketName(JsonNode storageSettings) throws Exception {
        LOGGER.trace("entering getBucketName()");
        final String bucketName = JsonNodeParser.getValueJsonNode(storageSettings, AMAZON_BUCKET_NAME_PARAM);

        LOGGER.trace("getBucketName(): bucketName {}", bucketName);
        if(StringUtils.isBlank(bucketName))
            throw new Exception("Bucket name was not found in settings");

        LOGGER.trace("leaving getBucketName(): bucketName in settings {}", bucketName);
        return bucketName;
    }

    private String uploadFile(JsonNode storageSettings, InputStream inputStream, Long contentLength, String mimeType, UUID uuid) throws Exception {
        LOGGER.trace("entering uploadFile(storageSettings={}, contentLength = {}, mimeType={}, uuid={})", storageSettings, contentLength, mimeType, uuid);

        final String bucketName = getBucketName(storageSettings);

        AmazonS3 amazonS3 = getAmazonS3(storageSettings);

        if(!isBucketExist(storageSettings, bucketName))
            createBucketWithName(amazonS3, bucketName);

        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        metadata.setContentType(mimeType);

        final String objectPathKey = formatObjectPathKey(uuid);

        amazonS3.putObject(
                new PutObjectRequest(bucketName, objectPathKey, inputStream, metadata));

        LOGGER.trace("leaving uploadFile(): uploaded objectPathKey {}", objectPathKey);
        return objectPathKey;
    }

    private boolean isBucketExist(JsonNode storageSettings, String bucketName) throws Exception {
        LOGGER.trace("entering isBucketExist(bucketName= {})", bucketName);
        final List<Bucket> buckets = getBuckets(storageSettings);
        LOGGER.trace("isBucketExist(): there is {} buckets in amazon",  buckets.size());
        for (Bucket bucket : buckets) {
            if(bucket.getName().equals(bucketName)){
                LOGGER.trace("leaving isBucketExist(): bucket with name {} was found in amazon", bucket.getName());
                return true;
            }
        }
        LOGGER.trace("leaving isBucketExist(): bucket with name {}, is not being exist in database", bucketName);
        return false;
    }

    private List<Bucket> getBuckets(JsonNode storageSettings) throws Exception {
        List<Bucket> bucketList = getAmazonS3(storageSettings).listBuckets();
        return bucketList != null ? bucketList : new ArrayList<>();
    }


    private String formatObjectPathKey(UUID uuid) {
        return uuid.toString();
    }

    private BasicAWSCredentials basicAWSCredentials(JsonNode storageSettings) throws Exception {
        LOGGER.trace("entering basicAWSCredentials(storageSettings={})", storageSettings);
        final String awsAccessKeyId = JsonNodeParser.getValueJsonNode(storageSettings, AMAZON_ACCESS_KEY_PARAM);
        final String awsSecretAccessKey =  JsonNodeParser.getValueJsonNode(storageSettings, AMAZON_ACCESS_SECRET_KEY_PARAM);
        LOGGER.trace("leaving basicAWSCredentials(): awsAccessKeyId = {}, awsSecretAccessKey = {}",
                awsAccessKeyId != null  ? awsAccessKeyId.length() : 0,
                awsSecretAccessKey != null ? awsSecretAccessKey.length() : 0);
        assert awsAccessKeyId != null;
        assert awsSecretAccessKey != null;
        return new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
    }

    private AmazonS3 getAmazonS3(JsonNode storagesettings) throws Exception {
        return amazonS3(basicAWSCredentials(storagesettings), storagesettings);
    }

    private AmazonS3 amazonS3(BasicAWSCredentials basicAWSCredentials, JsonNode storageSettings) throws Exception {
        LOGGER.trace("entering amazonS3(storageSettings = {})", storageSettings);
        final AmazonS3 amazonS3 = new AmazonS3Client(basicAWSCredentials);
        final String endpoint = JsonNodeParser.getValueJsonNode(storageSettings, AMAZON_ENDPOINT_PARAM);
        LOGGER.trace("amazonS3(): aws endpoint {}" , endpoint);

        amazonS3.setEndpoint(endpoint);

        amazonS3.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).build());
        LOGGER.trace("leaving amazonS3(): amazon3 was created");
        return amazonS3;
    }

//    private JsonNode getCurrentStorageSettingsNode() throws Exception {
//        LOGGER.trace("entering getCurrentStorageSettingsNode()");
//        final String currentStorageId = JsonNodeParser.getValueJsonNode(storageSettingsNode, AMAZON_CURRENT_STORAGE_ID_PARAM);
//
//        LOGGER.trace("getCurrentStorageSettingsNode(): currentStorageId {}", currentStorageId);
//
//        final JsonNode jsonNode = JsonNodeParser.getNodeByName(storageSettingsNode, currentStorageId);
//
//        if(jsonNode == null)
//            throw new Exception("Storage with such Id was not found in database");
//
//        return jsonNode;
//    }

}

