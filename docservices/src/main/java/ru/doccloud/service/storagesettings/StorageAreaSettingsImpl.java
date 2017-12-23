package ru.doccloud.service.storagesettings;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.doccloud.service.SystemCrudService;
import ru.doccloud.service.document.dto.SystemDTO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("storageAreaSettings")
public class StorageAreaSettingsImpl implements StorageAreaSettings {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageAreaSettingsImpl.class);

    private static final String DEFAULT_POLICY = "default_policy";

    private static final String STORAGE_POLICY = "storage_policy";

    private final SystemCrudService systemCrudService;

    private Map<String, Object> storage;

    @Autowired
    public StorageAreaSettingsImpl(SystemCrudService systemCrudService) {
        LOGGER.info("StorageAreaSettingsImpl(systemCrudService = {})", systemCrudService);
        this.systemCrudService = systemCrudService;
    }

    private Map<String, Object> getStorage() {
        if(storage == null)
            storage = new ConcurrentHashMap<>();
        return storage;
    }


    public JsonNode getSettingBySymbolicName(final String symbolicName) throws Exception {
        LOGGER.trace("entering getSettingBySymbolicName(symbolicName={}):  Try to find it in cache", symbolicName);

        final SystemDTO storageDoc = systemCrudService.findBySymbolicName(symbolicName);
        LOGGER.trace("leaving getSettingBySymbolicName(): storageDoc {} ", storageDoc);

        return storageDoc.getData();
    }

//    todo get StorageSettings by one query instead of three
    public JsonNode getStorageSettingsByType(final String docType) {

        LOGGER.trace("entering getStorageSettingsByType(docType={})", docType);
        final SystemDTO typeDoc = systemCrudService.findBySymbolicName(docType);

        LOGGER.debug("getStorageSettingsByType(): typedoc {}", typeDoc);
        final JsonNode data = typeDoc.getData().get(STORAGE_POLICY);

        LOGGER.trace("getStorageSettingsByType(): typedoc data {}", data);
        final String typePolicy = data != null ? typeDoc.getData().get(STORAGE_POLICY).asText() : DEFAULT_POLICY;

        LOGGER.trace("getStorageSettingsByType(): typePolicy {}", typePolicy);

        final SystemDTO policyDoc = systemCrudService.findBySymbolicName(typePolicy);
        LOGGER.trace("getStorageSettingsByType(): policydoc {}", policyDoc);
        final String storageArea = policyDoc.getData().get("storage_area").asText();
        LOGGER.trace("getStorageSettingsByType(): storageArea {}", storageArea);

        final SystemDTO storageDoc = systemCrudService.findBySymbolicName(storageArea);

        final JsonNode settingsNode = storageDoc.getData();
        LOGGER.debug("leaving getStorageSettingsByType(): settingsNode {}", settingsNode);
        return settingsNode;
    }

    @Override
    public String getStorageTypeByStorageName(String storageName) throws Exception {
        LOGGER.trace("entering getStorageSettingsByType(storageName={})", storageName);

        final SystemDTO settings = systemCrudService.findBySymbolicName(storageName);

        LOGGER.trace("leaving getStorageSettingsByType(): settings found {}", settings);
        return settings.getType();
    }

//    private JsonNode findSettingsInDatabase(final String settingsKey) throws Exception {
//        LOGGER.debug("findSettingsInDatabase(settingsKey={}): settings were not found in the cashe. Try to find it in database", settingsKey);
//        JsonNode settings = systemCrudService.findSettings(settingsKey);
//        if(settings == null)
//            throw new Exception("Storage area settings were not found in database");
//        LOGGER.debug("leaving findSettingsInDatabase(): Found {}", settings);
//        add(settingsKey, settings);
//
//        return settings;
//    }
}
