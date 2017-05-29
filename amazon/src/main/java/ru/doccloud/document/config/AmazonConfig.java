package ru.doccloud.document.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan
@PropertySource("classpath:application.properties")
public class AmazonConfig {

    private static final String AMAZON_KEY = "";
    private static final String SECRET_AMAZON_KEY = "";
    private static final String AMAZON_REGION = "doccloud";

//  @Bean
//  public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
//    return new PropertySourcesPlaceholderConfigurer();
//  }

//  todo get keys and regions from database after that uncommet
  // Amazon beans
//  @Bean
//  public BasicAWSCredentials basicAWSCredentials() {
//
//        String awsAccessKeyId = AMAZON_KEY;
//        String awsSecretAccessKey = SECRET_AMAZON_KEY;
//    return new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
//  }
//
//  @Bean(name = "amazonS3")
//  public AmazonS3 amazonS3(BasicAWSCredentials basicAWSCredentials) {
//      String awsRegion = AMAZON_REGION;
//    AmazonS3 amazonS3 = new AmazonS3Client(basicAWSCredentials);
//    amazonS3.setRegion(Region.getRegion(Regions.fromName(awsRegion)));
//    return amazonS3;
//  }


}
