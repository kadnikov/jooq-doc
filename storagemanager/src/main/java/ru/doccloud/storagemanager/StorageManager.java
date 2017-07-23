package ru.doccloud.storagemanager;

import com.fasterxml.jackson.databind.JsonNode;
import ru.doccloud.storage.StorageActionsService;


public interface StorageManager {
    public StorageActionsService getStorageService(Storages storage);

    public String getRootName (JsonNode settingsNode) throws Exception;

    public Storages getCurrentStorage(JsonNode settingsNode) throws Exception;
}
