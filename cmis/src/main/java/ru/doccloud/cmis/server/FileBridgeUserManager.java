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
import ru.doccloud.service.UserService;
import ru.doccloud.service.document.dto.UserDTO;

/**
 * Manages users for the FileShare repository.
 */
public class FileBridgeUserManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBridgeUserManager.class);

    private UserService userService;

    public FileBridgeUserManager(UserService userService) {
        this.userService = userService;
    }

    /**
     * Takes user and password from the CallContext and checks them.
     */
    public synchronized UserDTO authenticate(CallContext context) {
        // check user and password
        final UserDTO authentificatedUser = authenticate(context.getUsername(), context.getPassword());

        if (authentificatedUser == null) {
            throw new CmisPermissionDeniedException("Invalid username or password.");
        }

        return authentificatedUser;
    }

    /**
     * Authenticates a user against the configured logins.
     */
    private synchronized UserDTO authenticate(final String username, final String password) {
        //        todo remove logging after creating auth
        LOGGER.trace("authenticate(userName = {}, password={})", username, password);
        final UserDTO userDTO = userService.getUserDto(username, password);
        LOGGER.trace("authenticate(): userDto {}",userDTO);
        return userDTO;

    }
}