package ru.doccloud.document.config;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan
public class AmazonConfig {

    private static final String AMAZON_KEY = "";
    private static final String SECRET_AMAZON_KEY = "";
    private static final String AMAZON_REGION = "";

  @Bean
  public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

//  todo get keys and regions from database
  // Amazon beans
  @Bean
  public BasicAWSCredentials basicAWSCredentials() {

        String awsAccessKeyId = AMAZON_KEY;
        String awsSecretAccessKey = SECRET_AMAZON_KEY;
    return new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
  }

  @Bean
  public AmazonS3 amazonS3(BasicAWSCredentials basicAWSCredentials) {
      String awsRegion = AMAZON_REGION;
    AmazonS3 amazonS3 = new AmazonS3Client(basicAWSCredentials);
    amazonS3.setRegion(Region.getRegion(Regions.fromName(awsRegion)));
    return amazonS3;
  }


}
