package ru.doccloud.storage.storagesettings;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.doccloud.service.SystemCrudService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("storageAreaSettings")
public class StorageAreaSettingsImpl implements StorageAreaSettings {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageAreaSettingsImpl.class);

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

    private void add(String settingskey, Object object){
        getStorage().put(settingskey, object);
    }

    public Object getSetting(final String settingsKey) throws Exception {
        LOGGER.debug("getSetting(settingsKey={}):  Try to find it in cache", settingsKey);
        Object settings =  getStorage().get(settingsKey);
        LOGGER.debug("getSetting():  settings in cache {}", settings);
        if (settings != null)
            return settings;

        settings = findSettingsInDatabase(settingsKey);
        return settings;
    }

    private JsonNode findSettingsInDatabase(final String settingsKey) throws Exception {
        LOGGER.debug("findSettingsInDatabase(settingsKey={}): settings were not found in the cashe. Try to find it in database", settingsKey);
        JsonNode settings = systemCrudService.findSettings(settingsKey);
        if(settings == null)
            throw new Exception("Storage area settings were not found in database");
        LOGGER.debug("leaving findSettingsInDatabase(): Found {}", settings);
        add(settingsKey, settings);

        return settings;
    }
}
