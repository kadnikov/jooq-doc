package ru.doccloud.filestorage.repository;

import java.util.UUID;

/**
 * Created by ilya on 3/23/17.
 */
public interface FileRepository {
    public String writeFile(final String rootFolder, final UUID uuid, final byte[] fileArr) throws Exception;

    public byte[] readFile(final String filePath) throws Exception;
}
