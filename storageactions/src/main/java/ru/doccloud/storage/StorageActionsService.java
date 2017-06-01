package ru.doccloud.storage;


import java.util.UUID;

public interface StorageActionsService {
    /**
     *
     * @param rootName name either root folder for filerepository or bucket name for amazon S3
     * @param uuid identifier for save file under this name
     * @param fileArr byte array representation of file
     * @return Either pathToFile for file repository, or key for search in Aamzon
     * @throws Exception
     */
    public String writeFile(final String rootName, final UUID uuid, final byte[] fileArr) throws Exception;

    /**
     *
     * @param filePath Either path to file for fileRepository or key for search in AMAzon
     * @return file as byte array
     * @throws Exception
     */
    public byte[] readFile(final String filePath) throws Exception;
}
