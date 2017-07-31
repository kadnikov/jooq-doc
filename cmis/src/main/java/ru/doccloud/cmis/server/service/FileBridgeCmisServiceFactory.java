/*
 * Copyright 2014 Florian Müller & Jay Brown
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * This code is based on the Apache Chemistry OpenCMIS FileShare project
 * <http://chemistry.apache.org/java/developing/repositories/dev-repositories-fileshare.html>.
 *
 * It is part of a training exercise and not intended for production use!
 *
 */
package ru.doccloud.cmis.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.support.wrapper.CallContextAwareCmisService;
import org.apache.chemistry.opencmis.server.support.wrapper.CmisServiceWrapperManager;
import org.apache.chemistry.opencmis.server.support.wrapper.ConformanceCmisServiceWrapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.doccloud.cmis.server.FileBridgeCallContext;
import ru.doccloud.cmis.server.FileBridgeTypeManager;
import ru.doccloud.cmis.server.FileBridgeUserManager;
import ru.doccloud.cmis.server.repository.FileBridgeRepository;
import ru.doccloud.cmis.server.repository.FileBridgeRepositoryManager;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.common.global.SettingsKeys;
import ru.doccloud.common.util.JsonNodeParser;
import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.service.UserService;
import ru.doccloud.service.document.dto.UserDTO;
import ru.doccloud.storage.storagesettings.StorageAreaSettings;
import ru.doccloud.storagemanager.StorageManager;

import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.Map;

/**
 * FileShare Service Factory.
 */
@Service
public class FileBridgeCmisServiceFactory extends AbstractServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBridgeCmisServiceFactory.class);

    /** Default maxItems value for getTypeChildren()}. */
    private static final BigInteger DEFAULT_MAX_ITEMS_TYPES = BigInteger.valueOf(50);

    /** Default depth value for getTypeDescendants(). */
    private static final BigInteger DEFAULT_DEPTH_TYPES = BigInteger.valueOf(-1);

    /**
     * Default maxItems value for getChildren() and other methods returning
     * lists of objects.
     */
    private static final BigInteger DEFAULT_MAX_ITEMS_OBJECTS = BigInteger.valueOf(200);

    /** Default depth value for getDescendants(). */
    private static final BigInteger DEFAULT_DEPTH_OBJECTS = BigInteger.valueOf(10);

//    private final ApplicationContext appContext;

//    private final JTransfo transformer;

    private final DocumentCrudService crudService;
    private final StorageManager storageManager;
    private StorageAreaSettings storageAreaSettings;

    private final UserService userService;

    private ThreadLocal<CallContextAwareCmisService> threadLocalService = new ThreadLocal<CallContextAwareCmisService>();
    // new wrapperManager
    private CmisServiceWrapperManager wrapperManager;

    private FileBridgeRepositoryManager repositoryManager;
    private FileBridgeUserManager userManager;
    private FileBridgeTypeManager typeManager;
//    private UserInfo userInfo;

    @Autowired
    public FileBridgeCmisServiceFactory( DocumentCrudService crudService,
                                        StorageAreaSettings storageAreaSettings,  StorageManager storageManager,
                                        UserService userService) {
        LOGGER.info("FileBridgeCmisServiceFactory(crudService={}, storageAreaSettings= {}, storageManager={})", crudService, storageAreaSettings, storageManager);
//        this.appContext = appContext;
        this.crudService = crudService;
        this.storageAreaSettings = storageAreaSettings;
        this.storageManager = storageManager;
        this.userService = userService;
    }

    @Override
    public void init(Map<String, String> parameters) {
        LOGGER.trace("init(parameters={})", parameters);
        try {
            // New for Chameleon **
            wrapperManager = new CmisServiceWrapperManager();
            wrapperManager.addWrappersFromServiceFactoryParameters(parameters);
            wrapperManager.addOuterWrapper(ConformanceCmisServiceWrapper.class, DEFAULT_MAX_ITEMS_TYPES,
                    DEFAULT_DEPTH_TYPES, DEFAULT_MAX_ITEMS_OBJECTS, DEFAULT_DEPTH_OBJECTS);

            // *******
            // lets print out the parameters for debugging purposes so we can see
            // what happens to our
            // custom parameters
            if(LOGGER.isTraceEnabled()) {
                for (String currentKey : parameters.keySet()) {
                    LOGGER.info("init(): Key = {} -> Value = {}", currentKey, parameters.get(currentKey));
                }
            }

            repositoryManager = new FileBridgeRepositoryManager();
            userManager = new FileBridgeUserManager(userService);
            typeManager = new FileBridgeTypeManager();

        } catch (Exception e) {
            LOGGER.error("init(): exception {}", e);
        }
    }

    @Override
    public void destroy() {
        threadLocalService = null;
    }

    @Override
    public CmisService getService(CallContext context) {
        LOGGER.trace("getService(userName={})", context.getUsername());

        try {
    //        todo add authentificated user into the cache, it would be ideal if we used already authentificated user in application
            LOGGER.trace("getService(): userManager {}", userManager);
            UserDTO userDTO = userManager.authenticate(context);

    //        if login successfull, create cmis repository
            final JsonNode cmisSettings = (JsonNode)storageAreaSettings.getSetting(SettingsKeys.CMIS_SETTINGS_KEY.getSettingsKey());

            LOGGER.trace("getService(): cmisSettings {}", cmisSettings);
            final String repositoryId = JsonNodeParser.getValueJsonNode(cmisSettings, "repositoryId");
            LOGGER.trace("getService(): repositoryId {}", repositoryId);
            if(StringUtils.isBlank(repositoryId))
                throw new DocumentNotFoundException("Repository id was not found");

            LOGGER.trace("getService(): find repository from repositoryManager by repositoryId {}", repositoryId);

            FileBridgeRepository fsr = repositoryManager.getRepository(repositoryId);

            LOGGER.trace("getService(): foundRepository {} ", fsr);
            if(fsr == null) {
                final String rootPath = JsonNodeParser.getValueJsonNode(cmisSettings, "rootPath");
                LOGGER.trace("getService(): rootPath {}", rootPath);
                if(StringUtils.isBlank(rootPath))
                    throw new DocumentNotFoundException("root path was not found for repository " + repositoryId);
                fsr = new FileBridgeRepository(repositoryId, rootPath, typeManager, crudService, storageAreaSettings, storageManager, userService);
                repositoryManager.addRepository(fsr);
                LOGGER.trace("getService(): repository was creatd and added to repositoryManager {}", repositoryId);
            }

            // get service object for this thread
            CallContextAwareCmisService service = threadLocalService.get();
            if (service == null) {
                FileBridgeCmisService fileShareService = new FileBridgeCmisService(repositoryManager);
                // wrap it with the chain of wrappers
                service = (CallContextAwareCmisService) wrapperManager.wrap(fileShareService);
                threadLocalService.set(service);
            }

            // Stash any object into the call context and then pass it to our
            // service so that it can be shared with any extensions.
            // Here is where you would put in a reference to a native API object if
            // needed.
            FileBridgeCallContext fileBridgeCallContext = new FileBridgeCallContext(context);
            fileBridgeCallContext.setRequestTimestamp(new GregorianCalendar());

            service.setCallContext(fileBridgeCallContext);

            return service;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}