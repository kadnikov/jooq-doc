package ru.doccloud.storage.storagesettings;

import com.fasterxml.jackson.databind.JsonNode;

public interface StorageAreaSettings {
    JsonNode getSettingBySymbolicName(String symbolicName) throws Exception;

    JsonNode getStorageSettingsByType(String docType) throws Exception;

    String getStorageTypeByStorageName(String storageName) throws Exception;
}
