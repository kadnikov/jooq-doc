package ru.doccloud.amazon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.doccloud.amazon.repository.AmazonRepository;
import ru.doccloud.storage.StorageActionsService;

import java.util.UUID;

@Component("amazonActionsService")
public class AmazonServiceImpl implements StorageActionsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonServiceImpl.class);

    private final AmazonRepository amazonRepository;

    @Autowired
    public AmazonServiceImpl(AmazonRepository amazonRepository) {
        this.amazonRepository = amazonRepository;
    }


    @Override
    public String writeFile(String rootName, UUID uuid, byte[] fileArr) throws Exception {
        LOGGER.debug("entering writeFile(rootFolder={}, uuid={}, byte.lenght={})", rootName, uuid, fileArr.length);

        String pathToFile = amazonRepository.uploadFile(rootName, uuid, fileArr);;

        LOGGER.debug("leaving writeFile(): result {}", pathToFile);

        return pathToFile;
    }

    @Override
    public byte[] readFile(String filePath) throws Exception {
        LOGGER.debug("entering readFile(filePath={})", filePath);

        byte[] foundFile = amazonRepository.readFile(filePath);
        LOGGER.debug("leaving readFile(): found {}", foundFile.length);

        return foundFile;
    }


}

