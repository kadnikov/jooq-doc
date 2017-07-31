package ru.doccloud.document.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.common.global.SettingsKeys;
import ru.doccloud.service.document.dto.AbstractDocumentDTO;
import ru.doccloud.storage.StorageActionsService;
import ru.doccloud.storage.storagesettings.StorageAreaSettings;
import ru.doccloud.storagemanager.StorageManager;
import ru.doccloud.storagemanager.Storages;

import java.io.IOException;
import java.util.Enumeration;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by ilya on 6/5/17.
 */
abstract class AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractController.class);

    final StorageActionsService storageActionsService;

    private final StorageManager storageManager;
    private final DocumentCrudService crudService;
    JsonNode settingsNode;

    JsonNode storageSettingsNode;

    protected final Storages currentStorage;

    AbstractController(StorageAreaSettings storageAreaSettings, StorageManager storageManager, DocumentCrudService crudService) throws Exception {
        this.storageManager = storageManager;
        this.crudService = crudService;
        
        settingsNode = (JsonNode) storageAreaSettings.getStorageSetting();
        this.storageActionsService = storageManager.getStorageService(storageManager.getDefaultStorage(settingsNode));


        storageSettingsNode = (JsonNode) storageAreaSettings.getSetting(SettingsKeys.STORAGE_AREA_KEY.getSettingsKey());
        this.storageActionsService = storageManager.getStorageService(storageManager.getCurrentStorage(storageSettingsNode));

        currentStorage = storageManager.getCurrentStorage(storageSettingsNode);

    }

    void initFileParamsFromRequest(AbstractDocumentDTO dto, MultipartFile mpf) throws Exception {
        dto.setFileLength((long) mpf.getBytes().length);
        dto.setFileMimeType(mpf.getContentType());
        dto.setFileName(mpf.getOriginalFilename());
    }

    String writeContent(UUID uuid, byte[] bytes) throws Exception {
        return storageActionsService.writeFile(storageManager.getRootName(storageSettingsNode), uuid, bytes);
    }

    boolean checkMultipartFile(MultipartFile mpf) throws IOException {
        return !StringUtils.isBlank(mpf.getOriginalFilename()) &&
                !StringUtils.isBlank(mpf.getContentType()) &&
                mpf.getBytes().length > 0;
    }

    void populateFilePartDto(AbstractDocumentDTO dto, MultipartHttpServletRequest request, MultipartFile mpf) throws Exception {

        debugMultiPartRequestHeaderNames(request);
        String data = (String) request.getParameter("data");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jdata = mapper.readTree(data);
        String title = jdata.get("title").asText();
        String type = jdata.get("type").asText();
        dto.setTitle(title);
        dto.setType(type);

        initFileParamsFromRequest(dto, mpf);
    }


    void debugMultiPartRequestHeaderNames(MultipartHttpServletRequest request) {
        if(LOGGER.isDebugEnabled()) {
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
