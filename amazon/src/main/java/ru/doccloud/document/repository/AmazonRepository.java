package ru.doccloud.document.repository;

import java.util.UUID;

/**
 * @author Ilya Ushakov
 */

public interface AmazonRepository {

    public String uploadFile(String rootName, UUID uuid, byte[] fileArr);

    public byte[] readFile(String key) throws Exception;

    public String createBucket(final String bucketName);
    public void deleteBucket(final String bucketName);

}