package ru.doccloud.document.service;

import java.io.InputStream;

/**
 * @author Ilya Ushakov
 */

public interface AmazonService {

    public String uploadFile(String bucketName, InputStream inputStream,
                           Long contentLength, String mimeType, String key);

    void deleteFromAmazon(String bucketName, String objectKey);

    public byte[] readFile(String bucketName, String key) throws Exception;

}