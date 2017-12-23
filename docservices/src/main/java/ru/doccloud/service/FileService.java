package ru.doccloud.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;


public interface FileService {

    public String writeContent(UUID uuid, byte[] bytes, JsonNode settingsNode) throws Exception;

    public byte[] readFile(JsonNode storageSettings, String path) throws Exception;

    JsonNode getStorageSettingByStorageAreaName(String storageArea) throws Exception;

    public JsonNode getStorageSettingsByDocType(String docType) throws Exception;

}
