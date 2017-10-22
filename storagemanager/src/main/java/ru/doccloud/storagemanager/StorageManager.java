package ru.doccloud.storagemanager;

import ru.doccloud.storage.StorageActionsService;

public interface StorageManager {
    public StorageActionsService getStorageService(Storages storage);
}
