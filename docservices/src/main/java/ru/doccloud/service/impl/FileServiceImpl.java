package ru.doccloud.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.doccloud.common.util.JsonNodeParser;
import ru.doccloud.service.FileService;
import ru.doccloud.storage.StorageActionsService;
import ru.doccloud.service.storagesettings.StorageAreaSettings;
import ru.doccloud.storagemanager.StorageManager;
import ru.doccloud.storagemanager.Storages;

import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileServiceImpl.class);

	private StorageAreaSettings storageAreaSettings;

    private StorageManager storageManager;

    @Autowired
    public FileServiceImpl(StorageAreaSettings storageAreaSettings, StorageManager storageManager) {
        this.storageAreaSettings = storageAreaSettings;
        this.storageManager = storageManager;
    }

    @Override
	public String writeContent(UUID uuid, byte[] bytes, JsonNode storageSettings) throws Exception {
		LOGGER.trace("entering writeContent(uuid={}, bytes= {}, storageSettings={})", uuid, bytes.length, storageSettings);

		final StorageActionsService storageActionsService = getStorageActionServiceByStorageName(JsonNodeParser.getStorageAreaName(storageSettings));;

		final String pathToFile = storageActionsService.writeFile(storageSettings, uuid, bytes);

		LOGGER.trace("leaving writeContent(): pathTofile {}", pathToFile);
		return pathToFile;
	}

    @Override
    public byte[] readFile(JsonNode storageSettings, String path) throws Exception {
        LOGGER.trace("entering readFile(storageSettings={}, path= {}, )", storageSettings, path);

        StorageActionsService storageActionsService = getStorageActionService(storageSettings);

        byte[] file =  storageActionsService.readFile(storageSettings, path);
        LOGGER.trace("leaving readFile(): founded file {}", file != null ? file.length : "null");
        return file;
    }

	private StorageActionsService getStorageActionService(JsonNode storageSettings) throws Exception {
		return getStorageActionServiceByStorageName(JsonNodeParser.getStorageAreaName(storageSettings));
	}

	@Override
    public JsonNode getStorageSettingByStorageAreaName(String storageArea) throws Exception {
        LOGGER.debug("getStorageSettingByStorageAreaName(): docType: {}", storageArea);

        final JsonNode storageSetting = storageAreaSettings.getSettingBySymbolicName(storageArea);

        LOGGER.debug("getStorageSettingByStorageAreaName(): storageSetting: {}", storageSetting);

        return storageSetting;
    }


    @Override
    public JsonNode getStorageSettingsByDocType(String docType) throws Exception {
        return storageAreaSettings.getStorageSettingsByType(docType);
    }


    private StorageActionsService getStorageActionServiceByStorageName(final String storageName) throws Exception {

        LOGGER.trace("entering getStorageActionServiceByStorageName(storageName={})", storageName);
        final String storageType = storageAreaSettings.getStorageTypeByStorageName(storageName);

        LOGGER.trace("getStorageActionServiceByStorageName(): storageType {}", storageType);
        final Storages storage = Storages.getStorageByName(storageType);

        LOGGER.trace("getStorageActionServiceByStorageName(): storage {}", storage);
        if(storage == null)
            throw new Exception(String.format("current storage type %s is neither amazon nor filestorage", storageType));

        LOGGER.trace("getStorageActionServiceByStorageName(): storageType {}", storageType);

        StorageActionsService storageActionsService = storageManager.getStorageService(storage);

        LOGGER.trace("leaving getStorageActionServiceByStorageName(): storageActionsService {}", storageActionsService);
        return storageActionsService;
    }
}
