package ru.doccloud.filestorage.repository;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component("fileRepository")
public class FileRepositoryImpl implements FileRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileRepositoryImpl.class);
//    private static final String CONFIG_FILENAME = "/repository.properties";
//    private static final String FILE_PATH_PROPERTY = "repository.test";


    public String writeFile(final String rootFolder, final UUID uuid, final byte[] fileArr) throws Exception {
        LOGGER.trace("entering writeFile(rootFolder={}, uuid={}, byte.lenght={})", rootFolder, uuid, fileArr.length);
        try {
            if(StringUtils.isBlank(rootFolder) )
                throw new Exception("root folder is empty");

            final String[] folders = getFolderNames(uuid.toString());

            final String folderLvl1 = folders[0];
            final String folderLvl2 = folders[1];

            LOGGER.trace("writeFile(): folderLvl1 = {}, folderLvl2 = {}", folderLvl1, folderLvl2);
            String filePath = rootFolder +  "/" + folderLvl1;

            LOGGER.trace("writeFile(): folderLvl1 filePath = {}", filePath);
            Path path = Paths.get(filePath);
            if(Files.notExists(Paths.get(filePath), LinkOption.NOFOLLOW_LINKS)) {
                LOGGER.trace("writeFile(): directory with such filePath {} does not exist, it will be created", filePath);
                Files.createDirectories(path);
            }

            filePath = filePath + "/" + folderLvl2;

            LOGGER.trace("writeFile(): folderLvl2 filePath = {}", filePath);
            path = Paths.get(filePath);
            if(Files.notExists(Paths.get(filePath), LinkOption.NOFOLLOW_LINKS)) {
//                todo add recursion to for creating and checking file
                LOGGER.trace("writeFile(): directory with such filePath {} does not exist, it will be created", filePath);
                Files.createDirectories(path);
            }

            filePath = filePath + "/" + uuid;

            LOGGER.trace("writeFile(): filePath for file  {}", filePath );
            File file = new File(filePath);
            Files.write(file.toPath(), fileArr);

            LOGGER.trace("writeFile(): the file {} was written to {}", file.getName(), file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            LOGGER.error("Exception has been thrown while file was writing {} {}", e.getMessage(), e);

            throw new Exception("Exception has been thrown while file was writing " + e.getMessage());
        }

    }

    public byte[] readFile(final String filePath) throws Exception {
        return FileUtils.readFileToByteArray(new File(filePath));
    }

    private  String[] getFolderNames(String fileName){
        String foldersName = StringUtils.substring(fileName, 0, 4);

        final int mid = foldersName.length() / 2;
        return new String[]{foldersName.substring(0, mid),foldersName.substring(mid)};
    }

}
