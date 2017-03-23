package ru.doccloud.document.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.doccloud.document.controller.util.FileHelper;

import java.util.UUID;

@Service
public class FileActionsServiceImpl implements FileActionsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileActionsServiceImpl.class);

    private final FileHelper fileHelper;



    @Autowired
    public FileActionsServiceImpl(FileHelper fileHelper) {
        this.fileHelper = fileHelper;
    }

    @Override
    public String writeFile(final UUID uuid,  final byte[] fileArr) throws Exception {
        return fileHelper.writeFile(uuid, fileArr);
    }

    @Override
    public byte[] readFile(final String filePath) throws Exception {
        return fileHelper.readFile(filePath);
    }

}

