package ru.doccloud.document.amazon;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.Before;
import ru.doccloud.document.config.AmazonConfig;
import ru.doccloud.document.service.AmazonAdminService;
import ru.doccloud.document.service.AmazonAdminServiceImpl;

/**
 * Created by ilya on 5/27/17.
 */
public class TestAdminService {

    private final String bucketName = "doccloud-bucket";

    private AmazonAdminService amazonAdminService;

    @Before
    public void
    init(){
        AmazonConfig config = new AmazonConfig();
        AmazonS3 amazonS3 = config.amazonS3(config.basicAWSCredentials());
        amazonAdminService = new AmazonAdminServiceImpl(amazonS3);
    }

//    @Test
    public void testCreateBucket(){
        try {
            amazonAdminService.createBucket(bucketName);
        } catch (Exception e) {
            System.out.println("Exception " + e.getMessage());
            e.printStackTrace();

        }

    }
}
