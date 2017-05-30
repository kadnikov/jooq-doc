package ru.doccloud.document;

import ru.doccloud.document.storage.StorageActionsService;


public interface StorageManager {
    public StorageActionsService getStorageService(Storages storage);
}
