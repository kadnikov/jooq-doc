package ru.doccloud.amazon.service.admin;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * THis interface provides admin methods for amazon s3 interaction.
 * All methods connected to create/delete buckets, topics etc must be here
 */
public interface AmazonAdminService {
    public String createBucket(JsonNode storageSettings) throws Exception;
    public void deleteBucket(JsonNode storageSettings) throws Exception;
}
