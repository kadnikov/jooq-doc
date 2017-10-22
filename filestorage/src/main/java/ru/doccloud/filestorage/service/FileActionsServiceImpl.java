package ru.doccloud.filestorage.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.doccloud.common.util.JsonNodeParser;
import ru.doccloud.filestorage.repository.FileRepository;
import ru.doccloud.storage.StorageActionsService;

import java.util.UUID;

@Component("fileActionsService")
public class FileActionsServiceImpl implements StorageActionsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileActionsServiceImpl.class);

    private final FileRepository fileRepository;

    private static final String REPOSITORY_PARAM = "repository";

    @Autowired
    public FileActionsServiceImpl(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public String writeFile(JsonNode storageSettings, UUID uuid, byte[] fileArr) throws Exception {
        LOGGER.debug("entering writeFile(storageSettings={}, uuid={}, byte.lenght={})", storageSettings, uuid, fileArr.length);
        final String rootFolder = getRootFolder(storageSettings);
        LOGGER.debug("writeFile(): rootFolder = {}", rootFolder);
        String pathToFile = fileRepository.writeFile(rootFolder, uuid, fileArr);;

        LOGGER.debug("leaving writeFile(): result {}", pathToFile);

        return pathToFile;
    }

    @Override
    public byte[] readFile(JsonNode storageSettings, String filePath) throws Exception {
        final String rootFolder = getRootFolder(storageSettings);
        LOGGER.debug("writeFile(): rootFolder = {}", rootFolder);
        byte[] foundFile = fileRepository.readFile(filePath);
        LOGGER.debug("leaving readFile(): found {}", foundFile.length);

        return foundFile;
    }


    private String getRootFolder(JsonNode storageSettings) throws Exception {
        return JsonNodeParser.getValueJsonNode(storageSettings, REPOSITORY_PARAM);
    }

}

