package ru.doccloud.common.dto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ilya on 4/4/17.
 */
public enum StorageAreaSettings {
    INSTANCE;

    private static final String STORAGE_AREA_KEY = "storage_area";

    private Map<String, Object> storage;

    private Map<String, Object> getStorage() {
        if(storage == null)
            storage = new ConcurrentHashMap<>();
        return storage;
    }

    public void add(Object object){
        getStorage().put(STORAGE_AREA_KEY, object);
    }

    public Object getStorageSetting(){
        return getStorage().get(STORAGE_AREA_KEY);
    }
}
