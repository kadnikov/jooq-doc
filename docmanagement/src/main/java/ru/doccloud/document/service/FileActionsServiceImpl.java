package ru.doccloud.document.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.doccloud.document.repository.FileRepository;

import java.util.UUID;

@Service
public class FileActionsServiceImpl implements FileActionsService {

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

        LOGGER.debug("leaving writeFile(): found {}", pathToFile);

        return pathToFile;
    }

    @Override
    public byte[] readFile(final String filePath) throws Exception {
        LOGGER.debug("entering readFile(filePath={})", filePath);

        byte[] foundFile = fileRepository.readFile(filePath);
        LOGGER.debug("entering readFile(): found {}", foundFile.length);

        return foundFile;
    }

}

