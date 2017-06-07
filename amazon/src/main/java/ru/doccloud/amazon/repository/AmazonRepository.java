package ru.doccloud.amazon.repository;

import java.util.UUID;

/**
 * @author Ilya Ushakov
 */

public interface AmazonRepository {

    public String uploadFile(String rootName, UUID uuid, byte[] fileArr) throws Exception;

    public byte[] readFile(String key) throws Exception;

    public String createBucket(final String bucketName) throws Exception;
    public void deleteBucket(final String bucketName);

}