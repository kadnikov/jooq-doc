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
package ru.doccloud.cmis.server;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.support.wrapper.CallContextAwareCmisService;
import org.apache.chemistry.opencmis.server.support.wrapper.CmisServiceWrapperManager;
import org.apache.chemistry.opencmis.server.support.wrapper.ConformanceCmisServiceWrapper;
import org.jooq.DSLContext;
import org.jtransfo.JTransfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import ru.doccloud.config.PersistenceContext;

/**
 * FileShare Service Factory.
 */
@Service
public class FileBridgeCmisServiceFactory extends AbstractServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBridgeCmisServiceFactory.class);

    private static final String PREFIX_LOGIN = "login.";
    private static final String PREFIX_REPOSITORY = "repository.";
    private static final String SUFFIX_READWRITE = ".readwrite";
    private static final String SUFFIX_READONLY = ".readonly";

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
    
    @Autowired
    private ApplicationContext appContext;
    
    @Autowired
    private JTransfo transformer;
    
    /** Each thread gets its own {@link FileBridgeCmisService} instance. */
    // old threadLocalService
    // private ThreadLocal<CmisServiceWrapper<FileBridgeCmisService>>
    // threadLocalService = new
    // ThreadLocal<CmisServiceWrapper<FileBridgeCmisService>>();
    // new CallContextAware threadLocalService
    private ThreadLocal<CallContextAwareCmisService> threadLocalService = new ThreadLocal<CallContextAwareCmisService>();
    // new wrapperManager
    private CmisServiceWrapperManager wrapperManager;

    private FileBridgeRepositoryManager repositoryManager;
    private FileBridgeUserManager userManager;
    private FileBridgeTypeManager typeManager;

    @Override
    public void init(Map<String, String> parameters) {
        LOGGER.info("[FileBridgeCmisServiceFactory] init");
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
            for (String currentKey : parameters.keySet()) {
                LOGGER.info("[FileBridgeCmisServiceFactory]Key: " + currentKey + " ->Value:" + parameters.get(currentKey));
            }

            repositoryManager = new FileBridgeRepositoryManager();
            userManager = new FileBridgeUserManager();
            typeManager = new FileBridgeTypeManager();

            readConfiguration(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        threadLocalService = null;
    }

    @Override
    public CmisService getService(CallContext context) {
        
    	// LOGGER.info("[FileBridgeCmisServiceFactory] getService");
    	// authenticate the user
        // if the authentication fails, authenticate() throws a
        // CmisPermissionDeniedException
    	
        userManager.authenticate(context);

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
    }

    // ---- helpers ----

    /**
     * Reads the configuration and sets up the repositories, logins, and type
     * definitions.
     */
    private void readConfiguration(Map<String, String> parameters) throws Exception {
    	LOGGER.info("[FileBridgeCmisServiceFactory] readConfiguration");
        List<String> keys = new ArrayList<String>(parameters.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            if (key.startsWith(PREFIX_LOGIN)) {
                // get logins
                String usernameAndPassword = parameters.get(key);
                if (usernameAndPassword == null) {
                    continue;
                }

                String username = usernameAndPassword;
                String password = "";

                int x = usernameAndPassword.indexOf(':');
                if (x > -1) {
                    username = usernameAndPassword.substring(0, x);
                    password = usernameAndPassword.substring(x + 1);
                }

                LOGGER.info("Adding login '{}'.", username);

                userManager.addLogin(username, password);
            } else if (key.startsWith(PREFIX_REPOSITORY)) {
                // configure repositories
                String repositoryId = key.substring(PREFIX_REPOSITORY.length()).trim();
                int x = repositoryId.lastIndexOf('.');
                if (x > 0) {
                    repositoryId = repositoryId.substring(0, x);
                }

                if (repositoryId.length() == 0) {
                    throw new IllegalArgumentException("No repository id!");
                }

                if (key.endsWith(SUFFIX_READWRITE)) {
                    // read-write users
                    FileBridgeRepository fsr = repositoryManager.getRepository(repositoryId);
                    for (String user : split(parameters.get(key))) {
                        fsr.setUserReadWrite(user);
                    }
                } else if (key.endsWith(SUFFIX_READONLY)) {
                    // read-only users
                    FileBridgeRepository fsr = repositoryManager.getRepository(repositoryId);
                    for (String user : split(parameters.get(key))) {
                        fsr.setUserReadOnly(user);
                    }
                } else {
                    // new repository
                    String root = parameters.get(key);

                    LOGGER.info("Adding repository '{}': {}", repositoryId, root);
                    LOGGER.info("appContext: {}", appContext);
                    PersistenceContext pctx = appContext.getBean(PersistenceContext.class);
                    LOGGER.info("pctx: {}", pctx);
                    DSLContext jooq = pctx.dsl();
                    
                    FileBridgeRepository fsr = new FileBridgeRepository(repositoryId, root, typeManager, jooq, transformer);
                    repositoryManager.addRepository(fsr);
                }
            }
        }
    }

    /**
     * Splits a string by comma.
     */
    private List<String> split(String csl) {
        if (csl == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();
        for (String s : csl.split(",")) {
            result.add(s.trim());
        }

        return result;
    }
}
