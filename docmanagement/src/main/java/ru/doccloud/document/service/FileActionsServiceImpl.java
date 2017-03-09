package ru.doccloud.document.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.doccloud.document.controller.util.FileHelper;

@Service
public class FileActionsServiceImpl implements FileActionsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileActionsServiceImpl.class);

    private final FileHelper fileHelper;

    @Autowired
    public FileActionsServiceImpl(FileHelper fileHelper) {
        this.fileHelper = fileHelper;
    }

    @Override
    public String writeFile(String fileName, byte[] fileArr) throws Exception {
        return fileHelper.writeFile(fileName, fileArr);
    }

    @Override
    public byte[] readFile(String filePath) throws Exception {
        return fileHelper.readFile(filePath);
    }

//    private FileHelper getFileHelper(){
//        if(fileHelper == null) {
//
//            LOGGER.info("fileService wasn't being autowired, default constructor will be user for inintialistation");
//            fileHelper = new FileHelper();
//        }
//
//        return fileHelper;
//    }
}

