package ru.doccloud.document.service;

/**
 * THis interface provides admin methods for amazon s3 interaction.
 * All methods connected to create/delete buckets, topics etc must be here
 */
public interface AmazonAdminService {
    public String createBucket(final String bucketName);
    public void deleteBucket(final String bucketName);
}
