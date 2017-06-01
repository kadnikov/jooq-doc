package ru.doccloud.storage.storagesettings;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.doccloud.amazon.service.SystemCrudService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("storageAreaSettings")
public class StorageAreaSettingsImpl implements StorageAreaSettings {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageAreaSettingsImpl.class);

    private final SystemCrudService systemCrudService;

    private static final String STORAGE_AREA_KEY = "storage_area";

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

    private void add(Object object){
        getStorage().put(STORAGE_AREA_KEY, object);
    }

    public Object getStorageSetting() throws Exception {
        LOGGER.debug("getStorageSetting():  Try to find it in cache");
        Object settings =  getStorage().get(STORAGE_AREA_KEY);
        LOGGER.debug("getStorageSetting():  settings in cache {}", settings);
        if (settings != null)
            return settings;

        settings = findSettingsInDatabase();
        return settings;
    }

    private JsonNode findSettingsInDatabase() throws Exception {
        LOGGER.debug("findSettingsInDatabase(): settings were not found in the cashe. Try to find it in database");
        JsonNode settings = systemCrudService.findSettings();
        if(settings == null)
            throw new Exception("Storage area settings were not found in database");
        LOGGER.debug("leaving findSettingsInDatabase(): Found {}", settings);
        add(settings);

        return settings;
    }
}
