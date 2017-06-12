//package ru.doccloud.document.amazon;
//
//import com.amazonaws.services.s3.AmazonS3;
//import org.junit.Before;
//import org.junit.Test;
//import ru.doccloud.amazon.repository.AmazonReposiroryImpl;
//import ru.doccloud.amazon.repository.AmazonRepository;
//import ru.doccloud.amazon.service.AmazonServiceImpl;
//
///**
// * Created by ilya on 5/27/17.
// */
//public class TestAmazonService {
//
//    private final String filePath = "/home/ilya/Pictures/Screenshot from 2017-01-18 22-40-24.png";
//    private final String bucketName = "doccloud-bucket";
//
//
//    private AmazonServiceImpl amazonService;
//
//    @Before
//    public void
//    init(){
//        AmazonConfig config = new AmazonConfig();
//        AmazonS3 amazonS3 = config.amazonS3(config.basicAWSCredentials());
//        AmazonRepository amazonRepository = new AmazonReposiroryImpl();
//        amazonService = new AmazonServiceImpl(amazonS3);
//    }
//
//    @Test
//    public void testWriteFile(){
//
//        String filename = new File(filePath).getName();
//        try (FileInputStream fis = new FileInputStream(filePath)) {
//
//            String mime = map.getContentType(filename);
//            amazonService.uploadFile(bucketName, fis, (long) fis.available(), mime,
//                    filename);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//}
