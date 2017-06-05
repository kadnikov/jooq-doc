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
        LOGGER.info("StorageManagerImpl(): amazonActionsService {}, fileActionsService {}", amazonActionsService, fileActionsService);
        return storage.equals(Storages.AMAZONSTORAGE) ? amazonActionsService : fileActionsService;
    }

    @Override
    public String getRootName(JsonNode settingsNode) throws Exception {
        Storages currentStorage = getDefaultStorage(settingsNode);

        return JsonNodeParser.getValueJsonNode(settingsNode, currentStorage.equals(Storages.AMAZONSTORAGE) ? "bucketName": "repository");
    }

    public Storages getDefaultStorage(JsonNode settingsNode) throws Exception {

        String currentStorageId = JsonNodeParser.getValueJsonNode(settingsNode, "currentStorageID");

        LOGGER.debug("getDefaultStorage(): currentStorageId: {} ", currentStorageId);
        if(StringUtils.isBlank(currentStorageId))
            throw new Exception("StorageId is not set up");

        Storages storages = Storages.getStorageByName(currentStorageId);
        LOGGER.debug("getDefaultStorage(): Storages: {} ", storages);
        return storages;
    }
}
