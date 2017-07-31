package ru.doccloud.storagemanager;


import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.doccloud.common.util.JsonNodeParser;
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
        return (storage.equals(Storages.SCALITYAMAZONSTORAGE) || storage.equals(Storages.AMAZONSTORAGE)) ? amazonActionsService : fileActionsService;
    }

    @Override
    public String getRootName(JsonNode settingsNode) throws Exception {
        LOGGER.trace("entering getRootName(settingsNode= {}) ", settingsNode);
        final Storages currentStorage = getCurrentStorage(settingsNode);
        LOGGER.trace("leaving getRootName(): currentStorage ", currentStorage);
        return JsonNodeParser.getValueJsonNode(settingsNode, currentStorage.getRootName());
    }

    public Storages getCurrentStorage(JsonNode settingsNode) throws Exception {

        LOGGER.trace("entering getCurrentStorage(settingsNode= {}) ", settingsNode);
        String currentStorageId = JsonNodeParser.getValueJsonNode(settingsNode, "currentStorageID");

        LOGGER.trace("getCurrentStorage(): currentStorageId: {} ", currentStorageId);
        if(StringUtils.isBlank(currentStorageId))
            throw new Exception("StorageId is not set up");

        Storages storages = Storages.getStorageByName(currentStorageId);
        LOGGER.debug("leaving getCurrentStorage(): Storages: {} ", storages);
        return storages;
    }
}
