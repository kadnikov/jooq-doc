package ru.doccloud.document.amazon;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.StringUtils;
import org.junit.Before;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

/**
 * Created by ilya on 5/27/17.
 * http://docs.ceph.com/docs/master/radosgw/s3/java/
 */
public class TestAdminService {
    //
    private final String bucketName = "doccloud";

    private AmazonS3 amazonS3;

    @Before
    public void
    init(){
        AmazonConfig config = new AmazonConfig();
        amazonS3 = config.amazonS3(config.basicAWSCredentials());
    }

    //    @Test
    public void testCreateBucket(){
        Bucket bucket = amazonS3.createBucket(bucketName);
        System.out.println(bucket.getName() + "\t" +
                StringUtils.fromDate(bucket.getCreationDate()));
    }

//        @Test
    public void testGetBuckets(){
        List<Bucket> buckets = getBuckets();
        for (Bucket bucket : buckets) {
            System.out.println(bucket.getName() + "\t" +
                    StringUtils.fromDate(bucket.getCreationDate()));
        }
    }

    //    @Test
    public void testGetBucketLocation(){
        System.out.println("------------start testGetBucketLocation() ---------------");
        String location = amazonS3.getBucketLocation(bucketName);
        System.out.println("bucketLocation " + location);
        System.out.println("------------finish testGetBucketLocation() ---------------");
    }


//    @Test
    public void testBucketContent() {
        System.out.println("------------start testBucketContent() ---------------");
        List<Bucket> bucketList = getBuckets();
        System.out.println("bucketList " + bucketList);
        for(Bucket bucket: bucketList){
            System.out.println("current bucket " + bucket);
            ObjectListing objects = amazonS3.listObjects(bucket.getName());
            do {
                for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                    System.out.println(objectSummary.getKey() + "\t" +
                            objectSummary.getSize() + "\t" +
                            StringUtils.fromDate(objectSummary.getLastModified()));
                }
                objects = amazonS3.listNextBatchOfObjects(objects);
            } while (objects.isTruncated());
        }
        System.out.println("------------finish testBucketContent() ---------------");
    }

    //    @Test
    public void testSendFile() {
        System.out.println("------------start testSendFile() ---------------");
        final String filePath = "C:\\docs\\xquery-tutorial.pdf";
        MimetypesFileTypeMap map = new MimetypesFileTypeMap();

        String filename = new File(filePath).getName();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            String mime = map.getContentType(filename);
            System.out.println(String.format("Detected mime type %s", mime));
            String key = uploadFile(bucketName, fis, (long) fis.available(), mime,
                    filename);

            System.out.println(String.format("File with key \"%s\" was successfully sent to bucket \"%s\".",
                    key, bucketName));


        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("------------finish testSendFile() ---------------");
    }

//        @Test
    public void testDownloadObject(){
        System.out.println("------------start testDownloadObject() ---------------");
        final String objectKey = "98eff94a-34be-42c5-9056-51db581dc9a5";

        System.out.println("getDownload Link");
        S3Object s3Object = amazonS3.getObject(bucketName, objectKey);

        System.out.println("s3 object: " + s3Object.getObjectContent());
        try {
            byte[] byteArray = IOUtils.toByteArray(s3Object.getObjectContent());
            writeByteArrayToFile(byteArray, s3Object.getKey());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("------------finish testDownloadObject() ---------------");
    }

    //    @Test
    public void testChangeObjectAcl() {
        amazonS3.setObjectAcl(bucketName, "hello.txt", CannedAccessControlList.PublicRead);
        amazonS3.setObjectAcl(bucketName, "secret_plans.txt", CannedAccessControlList.Private);
    }



    //    @Test
    public void testDeleteObject(){
        final String objectKey = "2c5f3c8f-e8a5-483d-bda1-5f375dcdacab/xquery-tutorial.pdf";
        amazonS3.deleteObject(bucketName, objectKey);
    }

    //    @Test
    public void testGenerateDownloadUrl(){
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, "secret_plans.txt");
        System.out.println(amazonS3.generatePresignedUrl(request));
    }

    //    @Test
    public void testDeleteBucket(){
        /**
         * bucketscality	2017-06-12T12:18:58.227Z

         my-new-bucket	2017-06-07T20:24:19.244Z

         testbucket	2017-06-10T13:16:12.458Z
         */
        amazonS3.deleteBucket(bucketName);
    }

    private String uploadFile(String bucketName, InputStream inputStream,
                              Long contentLength, String mimeType, String key) {

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        metadata.setContentType(mimeType);

        String objectPathKey = formatObjectPathKey(key);

        amazonS3.putObject(
                new PutObjectRequest(bucketName, objectPathKey, inputStream, metadata));

        return objectPathKey;
    }

    private List<Bucket> getBuckets(){
        return amazonS3.listBuckets();
    }

    private String formatObjectPathKey(String objectKey) {
        return UUID.randomUUID() + "/" + objectKey;
    }

    private void writeByteArrayToFile(byte[] byteArray, String objKey) throws IOException {
        final String downloadPath = "/home/ilya/filenet_workspace/test_amazon";
//        String[] parts = objKey.split("/");

//        System.out.println("folder+file " + parts);
//
//        String folderPath = downloadPath+parts[0];
//        Path path = Paths.get(folderPath);
//        if(Files.notExists(Paths.get(folderPath), LinkOption.NOFOLLOW_LINKS)) {
//            Files.createDirectories(path);
//        }
        String filePath = downloadPath + "/" + objKey;
        File file = new File(filePath);
        Files.write(file.toPath(), byteArray);
    }
}
