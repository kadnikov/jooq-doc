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

import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages users for the FileShare repository.
 */
public class FileBridgeUserManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBridgeUserManager.class);

    private final Map<String, String> logins;

    public FileBridgeUserManager() {
        logins = new ConcurrentHashMap<>();
    }

    /**
     * Returns all logins.
     */
    public synchronized Collection<String> getLogins() {
        return logins.keySet();
    }

    /**
     * Adds a login.
     */
    public synchronized void addLogin(final String username, final String password) {
//        todo remove logging after creating auth
        LOGGER.info("addLogin(userName = {}, password={})", username, password);
        if (username == null || password == null) {
            return;
        }

        logins.put(username.trim(), password);
    }

    /**
     * Takes user and password from the CallContext and checks them.
     */
    public synchronized String authenticate(CallContext context) {
        // check user and password
        if (!authenticate(context.getUsername(), context.getPassword())) {
            throw new CmisPermissionDeniedException("Invalid username or password.");
        }

        return context.getUsername();
    }

    /**
     * Authenticates a user against the configured logins.
     */
    private synchronized boolean authenticate(final String username, final String password) {
        //        todo remove logging after creating auth
        LOGGER.info("authenticate(userName = {}, password={})", username, password);
        final String pwd = logins.get(username);
        return pwd != null && pwd.equals(password);

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (String user : logins.keySet()) {
            sb.append('[');
            sb.append(user);
            sb.append(']');
        }

        return sb.toString();
    }
}
