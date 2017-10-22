package ru.doccloud.storagemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.doccloud.storage.StorageActionsService;

import javax.annotation.Resource;


@Component("storageManager")
public class StorageManagerImpl implements StorageManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageManagerImpl.class);

    @Resource(name = "fileActionsService")
    private final
    StorageActionsService fileActionsService;

    @Resource(name = "amazonActionsService")
    private final
    StorageActionsService amazonActionsService;

    @Autowired
    public StorageManagerImpl(StorageActionsService fileActionsService, StorageActionsService amazonActionsService) {
        this.fileActionsService = fileActionsService;
        this.amazonActionsService = amazonActionsService;
    }

    public StorageActionsService getStorageService(Storages storage){
        LOGGER.trace("getStorageService(): amazonActionsService {}, fileActionsService {}", amazonActionsService, fileActionsService);
        return ( storage.equals(Storages.AMAZONSTORAGE)) ? amazonActionsService : fileActionsService;
    }
}
