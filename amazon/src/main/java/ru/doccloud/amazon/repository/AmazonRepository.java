package ru.doccloud.amazon.repository;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

/**
 * @author Ilya Ushakov
 */

public interface AmazonRepository {

    public String uploadFile(JsonNode settingsNode, UUID uuid, byte[] fileArr) throws Exception;

    public byte[] readFile(JsonNode settingsNode, String key) throws Exception;

    public String createBucket(JsonNode storageSettings) throws Exception;
    public void deleteBucket(JsonNode storageSettings) throws Exception;

}