package ru.doccloud.document;

import ru.doccloud.storage.StorageActionsService;


public interface StorageManager {
    public StorageActionsService getStorageService(Storages storage);
}
