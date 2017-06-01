package ru.doccloud.filestorage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.doccloud.filestorage.repository.FileRepository;
import ru.doccloud.storage.StorageActionsService;

import java.util.UUID;

@Component("fileActionsService")
public class FileActionsServiceImpl implements StorageActionsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileActionsServiceImpl.class);

    private final FileRepository fileRepository;

    @Autowired
    public FileActionsServiceImpl(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public String writeFile(final String rootFolder, final UUID uuid,  final byte[] fileArr) throws Exception {
        LOGGER.debug("entering writeFile(rootFolder={}, uuid={}, byte.lenght={})", rootFolder, uuid, fileArr.length);

        String pathToFile = fileRepository.writeFile(rootFolder, uuid, fileArr);;

        LOGGER.debug("leaving writeFile(): result {}", pathToFile);

        return pathToFile;
    }

    @Override
    public byte[] readFile(final String filePath) throws Exception {
        LOGGER.debug("entering readFile(filePath={})", filePath);

        byte[] foundFile = fileRepository.readFile(filePath);
        LOGGER.debug("leaving readFile(): found {}", foundFile.length);

        return foundFile;
    }

}

