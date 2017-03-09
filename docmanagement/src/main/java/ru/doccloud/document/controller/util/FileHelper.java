package ru.doccloud.document.controller.util;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.doccloud.common.util.PropertyReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component("fileHelper")
public class FileHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileHelper.class);
    private static final String CONFIG_FILENAME = "/repository.properties";
    private static final String FILE_PATH_PROPERTY = "repository.test";

    public String writeFile(final String fileName, final byte[] fileArr) throws Exception {
        LOGGER.info("writing file " + fileName + " to filesystem");
        try {
//            todo expand functionality to write to remote server
            final String directoryPath = PropertyReader.getProperty(CONFIG_FILENAME, FILE_PATH_PROPERTY);
            if(StringUtils.isBlank(directoryPath) )
                throw new Exception("property file " + CONFIG_FILENAME + " or such property " + FILE_PATH_PROPERTY + " does not exist");

            String fileNameWithoutExt = FilenameUtils.removeExtension(fileName);
            LOGGER.debug("THe file name without extenssion " + fileNameWithoutExt + " will be split into two parts");
            final String[] folders = getFolderNames(fileNameWithoutExt);

            final String folderLvl1 = folders[0];
            final String folderLvl2 = folders[1];

            String filePath = directoryPath +  "/" + folderLvl1;

            Path path = Paths.get(filePath);
            if(Files.notExists(Paths.get(filePath), LinkOption.NOFOLLOW_LINKS)) {
//                todo add recursion to for creating and checking file
                LOGGER.debug("The folder " + filePath + " does not exist. Folder will be created");
                Files.createDirectories(path);
            }

            filePath = filePath + "/" + folderLvl2;
            path = Paths.get(filePath);
            if(Files.notExists(Paths.get(filePath), LinkOption.NOFOLLOW_LINKS)) {
//                todo add recursion to for creating and checking file
                LOGGER.debug("The folder " + filePath + " does not exist. Folder will be created");
                Files.createDirectories(path);
            }


            LOGGER.debug("The filePath for file  " + filePath );
            File file = new File(filePath + "/" + fileName);

            Files.write(file.toPath(), fileArr);

            LOGGER.debug("The file was written to path obj  ");
//            path = Files.createFile(path);
//            FileUtils.writeByteArrayToFile(file, fileArr);
            LOGGER.info("the file " + fileName + " was written to " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            LOGGER.error("Exception has been thrown while file was writing " + e.getMessage());

            e.printStackTrace();
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

    private boolean checkWritableDirectory(Path path) throws Exception {
        if(Files.isWritable(path))
            return true;
        else {
            LOGGER.error("The directory: {} is not writable, please check user rights for writing", path.getFileName());
            throw new Exception("The directory " + path.getFileName() + "is not writable please check user rigths");
        }
    }
}
