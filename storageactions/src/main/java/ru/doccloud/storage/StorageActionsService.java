package ru.doccloud.storage;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public interface StorageActionsService {

    /**
     *
     * @param storageSettings json contains storage settings
     * @param uuid identifier for save file under this name
     * @param fileArr byte array representation of file
     * @return Either pathToFile for file repository, or key for search in Aamzon
     * @throws Exception
     */
    public String writeFile(JsonNode storageSettings, final UUID uuid, final byte[] fileArr) throws Exception;

    /**
     *@param storageSettings json contains storage settings
     * @param filePath Either path to file for fileRepository or key for search in AMAzon
     * @return file as byte array
     * @throws Exception
     */
    public byte[] readFile(JsonNode storageSettings, final String filePath) throws Exception;

}
