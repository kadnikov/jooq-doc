package ru.doccloud.document.amazon;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

/**
 * Created by ilya on 5/27/17.
 * http://docs.ceph.com/docs/master/radosgw/s3/java/
 */
public class TestAdminService {
//
    private final String bucketName = "test-bucket";

    private AmazonS3 amazonS3;

    @Before
    public void
    init(){
        AmazonConfig config = new AmazonConfig();
        amazonS3 = config.amazonS3(config.basicAWSCredentials());
    }

//    @Test
    public void testCreateBucket(){
        System.out.println("--------------------starting testCreateBucket----------");
        Bucket bucket = amazonS3.createBucket(bucketName);
        System.out.println(bucket.getName() + "\t" +
                StringUtils.fromDate(bucket.getCreationDate()));

        System.out.println("--------------------finishing testCreateBucket----------");
    }



//    @Test
    public void testGetBuckets(){
        System.out.println("--------------------starting testGetBuckets test----------");
        List<Bucket> buckets = getBuckets();
        for (Bucket bucket : buckets) {
            System.out.println(bucket.getName() + "\t" +
                    StringUtils.fromDate(bucket.getCreationDate()));
        }
        System.out.println("--------------------finishing testGetBuckets test----------");
    }

    @Test
    public void testBucketContent() {
        System.out.println("--------------------starting testBucketContent test----------");
        List<Bucket> bucketList = getBuckets();
        System.out.println("bucket list: " + bucketList);
        for(Bucket bucket: bucketList){
            final String bucketName = bucket.getName();
            System.out.println("current bucket: " + bucketName);
            ObjectListing objects = amazonS3.listObjects(bucketName);

            do {
                for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                    System.out.println(objectSummary.getKey() + "\t" +
                            objectSummary.getSize() + "\t" +
                            StringUtils.fromDate(objectSummary.getLastModified()));
                }
                objects = amazonS3.listNextBatchOfObjects(objects);
            } while (objects.isTruncated());
        }
        System.out.println("--------------------finishing testBucketContent test----------");
    }

//    @Test
    public void testCreateObject() {
        ByteArrayInputStream input = new ByteArrayInputStream("Hello World!".getBytes());
        amazonS3.putObject(bucketName, "hello.txt", input, new ObjectMetadata());
    }

//    @Test
    public void testChangeObjectAcl() {
        amazonS3.setObjectAcl(bucketName, "hello.txt", CannedAccessControlList.PublicRead);
        amazonS3.setObjectAcl(bucketName, "secret_plans.txt", CannedAccessControlList.Private);
    }

//    @Test
    public void testDownloadObject(){
        amazonS3.getObject(
                new GetObjectRequest(bucketName, "perl_poetry.pdf"),
                new File("/home/ilya/test.pdf")
        );

    }

//    @Test
    public void testDeleteObject(){
        amazonS3.deleteObject(bucketName, "goodbye.txt");
    }

//    @Test
    public void testGenerateDovnloadUrl(){
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, "secret_plans.txt");
        System.out.println(amazonS3.generatePresignedUrl(request));
    }
    
//    @Test
    public void testDeleteBucket(){
        amazonS3.deleteBucket(bucketName);
    }

    private List<Bucket> getBuckets(){
        return amazonS3.listBuckets();
    }

}
