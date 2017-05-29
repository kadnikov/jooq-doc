package ru.doccloud.document.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class StorageConfig {

//    @Bean(name = "fileRepository")
//    FileRepository fileRepository(){
//        return new FileRepositoryImpl();
//    }
//
//    @Bean(name = "amazonRepository")
//    AmazonRepository amazonReposirory(){
//        return new AmazonReposiroryImpl();
//    }
//
//    @Bean(name = "fileActionsService")
//    StorageActionsService fileActionsService() {
//        return new FileActionsServiceImpl(fileRepository());
//    }
//
//    @Bean(name = "amazonActionsService")
//    StorageActionsService amazonActionsService() {
//        return new AmazonServiceImpl(amazonReposirory());
//    }


}
