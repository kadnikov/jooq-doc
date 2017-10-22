package ru.doccloud.amazon.service;

import com.fasterxml.jackson.databind.JsonNode;
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
    public String writeFile(JsonNode storageSettings, UUID uuid, byte[] fileArr) throws Exception {
        LOGGER.debug("entering writeFile(storageSettings={}, uuid={}, byte.length={})", storageSettings, uuid, fileArr.length);

        String pathToFile = amazonRepository.uploadFile(storageSettings, uuid, fileArr);

        LOGGER.debug("leaving writeFile(): result {}", pathToFile);
        return pathToFile;
    }

    @Override
    public byte[] readFile(JsonNode storageSettings, String filePath) throws Exception {
        LOGGER.debug("entering readFile(storageSettings={}, filePath={})", storageSettings, filePath);

        byte[] foundFile = amazonRepository.readFile(storageSettings, filePath);
        LOGGER.debug("leaving readFile(): found {}", foundFile.length);

        return foundFile;
    }
}

