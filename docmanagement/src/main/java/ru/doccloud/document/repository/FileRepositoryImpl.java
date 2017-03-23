package ru.doccloud.document.repository;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.doccloud.common.util.PropertyReader;

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
    private static final String CONFIG_FILENAME = "/repository.properties";
    private static final String FILE_PATH_PROPERTY = "repository.test";


    public String writeFile(final UUID uuid, final byte[] fileArr) throws Exception {
        LOGGER.debug("writing file {} to filesystem", uuid);
        try {
//            todo expand functionality to write to remote server
            final String directoryPath = PropertyReader.getProperty(CONFIG_FILENAME, FILE_PATH_PROPERTY);
            if(StringUtils.isBlank(directoryPath) )
                throw new Exception("property file " + CONFIG_FILENAME + " or such property " + FILE_PATH_PROPERTY + " does not exist");

            final String[] folders = getFolderNames(uuid.toString());

            final String folderLvl1 = folders[0];
            final String folderLvl2 = folders[1];

            String filePath = directoryPath +  "/" + folderLvl1;

            Path path = Paths.get(filePath);
            if(Files.notExists(Paths.get(filePath), LinkOption.NOFOLLOW_LINKS)) {
//                todo add recursion to for creating and checking file

                Files.createDirectories(path);
            }

            filePath = filePath + "/" + folderLvl2;
            path = Paths.get(filePath);
            if(Files.notExists(Paths.get(filePath), LinkOption.NOFOLLOW_LINKS)) {
//                todo add recursion to for creating and checking file
                LOGGER.debug("The folder {} does not exist. Folder will be created", filePath);
                Files.createDirectories(path);
            }


            filePath = filePath + "/" + uuid;

            LOGGER.debug("The filePath for file  {}", filePath );
            File file = new File(filePath);
            Files.write(file.toPath(), fileArr);

            LOGGER.info("the file {} was written to {} {}", file.getName(), file.getAbsolutePath());
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
        final int mid = fileName.length() / 2;
        return new String[]{fileName.substring(0, mid),fileName.substring(mid)};
    }

}
