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

  @Bean
  public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

//  todo get keys and regions from database
  // Amazon beans
  @Bean
  public BasicAWSCredentials basicAWSCredentials() {

        String awsAccessKeyId = "AKIAJWB5GOLHL3TLCVMA";
        String awsSecretAccessKey = "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";
    return new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
  }

  @Bean
  public AmazonS3 amazonS3(BasicAWSCredentials basicAWSCredentials) {
      String awsRegion = "doccloud";
    AmazonS3 amazonS3 = new AmazonS3Client(basicAWSCredentials);
    amazonS3.setRegion(Region.getRegion(Regions.fromName(awsRegion)));
    return amazonS3;
  }


}
