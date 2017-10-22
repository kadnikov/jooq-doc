package ru.doccloud.document.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import ru.doccloud.common.util.JsonNodeParser;
import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.service.document.dto.AbstractDocumentDTO;
import ru.doccloud.storage.StorageActionsService;
import ru.doccloud.storage.storagesettings.StorageAreaSettings;
import ru.doccloud.storagemanager.StorageManager;
import ru.doccloud.storagemanager.Storages;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.UUID;


abstract class AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractController.class);

    private final StorageManager storageManager;
    private final DocumentCrudService crudService;

    private StorageAreaSettings storageAreaSettings;

    AbstractController(StorageAreaSettings storageAreaSettings, StorageManager storageManager, DocumentCrudService crudService) throws Exception {
        this.storageManager      = storageManager;
        this.crudService         = crudService;
        this.storageAreaSettings = storageAreaSettings;
    }

    private void initFileParamsFromRequest(AbstractDocumentDTO dto, MultipartFile mpf) throws Exception {
        LOGGER.debug("entering initFileParamsFromRequest(fileLength = {}, contentType={}, fileName={})",
                mpf.getBytes().length, mpf.getContentType(), mpf.getOriginalFilename());

        dto.setFileLength((long) mpf.getBytes().length);
        dto.setFileMimeType(mpf.getContentType());
        dto.setFileName(mpf.getOriginalFilename());
        dto.setTitle(FilenameUtils.removeExtension(mpf.getOriginalFilename()));
        LOGGER.debug("leaving initFileParamsFromRequest(): dto={}", dto);
    }

    String writeContent(UUID uuid, byte[] bytes, JsonNode storageSettings) throws Exception {
        LOGGER.trace("entering writeContent(uuid={}, bytes= {}, settingsNode={})", uuid, bytes.length, storageSettings);

        final StorageActionsService storageActionsService = getStorageActionService(storageSettings);

        LOGGER.trace("writeContent(): storageActionsService {}", storageSettings);

        final String pathToFile = storageActionsService.writeFile(storageSettings, uuid, bytes);

        LOGGER.trace("leaving writeContent(): pathTofile {}", pathToFile);
        return pathToFile;
    }

    boolean checkMultipartFile(MultipartFile mpf) throws IOException {
        return !StringUtils.isBlank(mpf.getOriginalFilename()) &&
                !StringUtils.isBlank(mpf.getContentType()) &&
                mpf.getBytes().length > 0;
    }

    void populateFilePartDto(AbstractDocumentDTO dto, MultipartHttpServletRequest request, MultipartFile mpf) throws Exception {

        LOGGER.debug("entering populateFilePartDto(dto={}, request= {}, mpf={})", dto, request, mpf);

        debugMultiPartRequestHeaderNames(request);

        initFileParamsFromRequest(dto, mpf);

        LOGGER.debug("leaving populateFilePartDto(): dto={}", dto);
    }

     StorageActionsService getStorageActionService(JsonNode storageSettings) throws Exception {
        return getStorageActionServiceByStorageName(getStorageAreaName(storageSettings));
     }

     String getStorageAreaName(JsonNode storageSettings) throws Exception {
         LOGGER.trace("entering getStorageAreaName(storageSettings= {})", storageSettings);
         final String storageName = JsonNodeParser.getValueJsonNode(storageSettings, "symbolicName");
         LOGGER.trace("leaving getStorageAreaName(): storageName {}", storageName);

         return storageName;
     }

    StorageActionsService getStorageActionServiceByStorageName(final String storageName) throws Exception {

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

    JsonNode getStorageSetting(String docType) throws Exception {
        LOGGER.debug("getContent(): docType: {}", docType);

        final JsonNode storageSetting = getStorageSettingsByDocType(docType);

        LOGGER.debug("getContent(): storageSetting: {}", storageSetting);

        return storageSetting;
    }

    JsonNode getStorageSettingByStorageAreaName(String storageArea) throws Exception {
        LOGGER.debug("getStorageSettingByStorageAreaName(): docType: {}", storageArea);

        final JsonNode storageSetting = storageAreaSettings.getSettingBySymbolicName(storageArea);

        LOGGER.debug("getStorageSettingByStorageAreaName(): storageSetting: {}", storageSetting);

        return storageSetting;
    }

    private JsonNode getStorageSettingsByDocType(String docType) throws Exception {
        return storageAreaSettings.getStorageSettingsByType(docType);
    }

    private void debugMultiPartRequestHeaderNames(MultipartHttpServletRequest request) {

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("debugMultiPartRequestHeaderNames(): getting all header parameters");
            Enumeration<String> headerNames = request.getParameterNames();//.getAttribute("data");

            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    LOGGER.debug("Header: " + headerName + " - " + request.getHeader(headerName));
                }
            }
        }
    }

    void setUser(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        LOGGER.info("httpservlet request from setUser {} ", request);
        if(request == null)
            crudService.setUser();
        else
            crudService.setUser(request.getRemoteUser());
    }
}
