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

import org.apache.chemistry.opencmis.commons.data.*;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.server.support.wrapper.CallContextAwareCmisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.doccloud.cmis.server.repository.FileBridgeRepository;
import ru.doccloud.cmis.server.repository.FileBridgeRepositoryManager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * FileShare Service implementation.
 */
//@Component
//@Scope(value = "request")
public class FileBridgeCmisService extends AbstractCmisService implements CallContextAwareCmisService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileBridgeCmisService.class);

    private final FileBridgeRepositoryManager repositoryManager;
    private CallContext context;

    //    @Autowired
    FileBridgeCmisService(FileBridgeRepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }


    // --- Call Context ---

    /**
     * Sets the call context.
     *
     * This method should only be called by the service factory.
     */
    public void setCallContext(CallContext context) {
        this.context = context;
    }

    /**
     * Gets the call context.
     */
    public CallContext getCallContext() {
        return context;
    }

    /**
     * Gets the repository for the current call.
     */
    public FileBridgeRepository getRepository() {
        LOGGER.debug("getRepository()");
        return repositoryManager.getRepository(getCallContext().getRepositoryId());
    }

    // --- repository service ---

    @Override
    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
        LOGGER.debug("getRepositoryInfo(repositoryId = {}, extension={})", repositoryId, extension);
        for (FileBridgeRepository fsr : repositoryManager.getRepositories()) {
            if (fsr.getRepositoryId().equals(repositoryId)) {
                LOGGER.debug("getRepositoryInfo(): found repository {}", fsr);
                try {
                    return fsr.getRepositoryInfo(getCallContext());
                } catch (IllegalAccessException e) {
                    LOGGER.error("getRepositoryInfo(): Exception {}", e);
                }
            }
        }

        throw new CmisObjectNotFoundException("Unknown repository '" + repositoryId + "'!");
    }

    @Override
    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
        LOGGER.debug("getRepositoryInfos(extension={})", extension);
        List<RepositoryInfo> result = new ArrayList<>();

        for (FileBridgeRepository fsr : repositoryManager.getRepositories()) {
            try {
                LOGGER.debug("getRepositoryInfos(): found repository {}", fsr);
                result.add(fsr.getRepositoryInfo(getCallContext()));
            } catch (IllegalAccessException e) {
                LOGGER.error("getRepositoryInfos(): Exception {}", e);
            }
        }

        return result;
    }

    @Override
    public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
                                              BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return getRepository().getTypeChildren(getCallContext(), typeId, includePropertyDefinitions, maxItems,
                skipCount);
    }

    @Override
    public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
        return getRepository().getTypeDefinition(getCallContext(), typeId);
    }

    // @Override
    // public List<TypeDefinitionContainer> getTypeDescendants(String
    // repositoryId, String typeId, BigInteger depth,
    // Boolean includePropertyDefinitions, ExtensionsData extension) {
    // return getRepository().getTypeDescendants(getCallContext(), typeId,
    // depth, includePropertyDefinitions);
    // }

    // --- navigation service ---

    @Override
    public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
                                          Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
                                          Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return getRepository().getChildren(getCallContext(), folderId, filter, orderBy, includeAllowableActions,
                includePathSegment, maxItems, skipCount, this);
    }

    @Override
    public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth,
                                                        String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
                                                        String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        try {
            return getRepository().getDescendants(getCallContext(), folderId, depth, filter, includeAllowableActions,
                    includePathSegment, this, false);
        } catch (IOException e) {
            LOGGER.error("getDescendants(): Exception {}", e);
        }
        return null;
    }

    @Override
    public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {
        return getRepository().getFolderParent(getCallContext(), folderId, filter, this);
    }

    @Override
    public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth,
                                                       String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
                                                       String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        try {
            return getRepository().getDescendants(getCallContext(), folderId, depth, filter, includeAllowableActions,
                    includePathSegment, this, true);
        } catch (IOException e) {
            LOGGER.error("getFolderTree(): Exception {}", e);
        }
        return null;
    }

    @Override
    public List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
                                                   Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
                                                   Boolean includeRelativePathSegment, ExtensionsData extension) {
        return getRepository().getObjectParents(getCallContext(), objectId, filter, includeAllowableActions,
                includeRelativePathSegment, this);
    }

    @Override
    public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
                                        Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
                                        BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        ObjectListImpl result = new ObjectListImpl();
        result.setHasMoreItems(false);
        result.setNumItems(BigInteger.ZERO);
        List<ObjectData> emptyList = Collections.emptyList();
        result.setObjects(emptyList);

        return result;
    }

    // --- object service ---

    @Override
    public String create(String repositoryId, Properties properties, String folderId, ContentStream contentStream,
                         VersioningState versioningState, List<String> policies, ExtensionsData extension) {

        LOGGER.info("entering create()",
                repositoryId, properties, folderId, versioningState, policies, extension);
        try {
            ObjectData object = getRepository().create(getCallContext(), properties, folderId, contentStream,
                    versioningState, this);

            LOGGER.info("leaving create()");
            return object.getId();
        } catch (Exception e) {
            LOGGER.error("create(): Exception {}", e);
        }

        return null;
    }

    @Override
    public String createDocument(String repositoryId, Properties properties, String folderId,
                                 ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces,
                                 Acl removeAces, ExtensionsData extension)  {

        LOGGER.info("entering createDocument(repositoryId={}, properties = {}, folderId ={})", repositoryId, properties, folderId);
        try {
            ObjectData object = getRepository().create(getCallContext(), properties, folderId, contentStream,
                    versioningState, this);
            LOGGER.info("leaving createDocument()");
            return object.getId();
        } catch (Exception e) {
            LOGGER.error("createDocument(): Exception {}", e);
        }
        return null;
    }

    @Override
    public String createDocumentFromSource(String repositoryId, String sourceId, Properties properties,
                                           String folderId, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces,
                                           ExtensionsData extension) {
        try {
            LOGGER.info(" createDocumentFromSource()");
            return getRepository().createDocumentFromSource(getCallContext(), sourceId, properties, folderId,
                    versioningState);
        } catch (Exception e) {
            LOGGER.error("createDocumentFromSource(): Exception {}", e);
        }
        return null;
    }

    @Override
    public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies,
                               Acl addAces, Acl removeAces, ExtensionsData extension) {
        LOGGER.info("entering createFolder()");
        try {
            ObjectData object = getRepository().create(getCallContext(), properties, folderId, null,
                    null, this);
            LOGGER.info("leaving createFolder()");
            return object.getId();
        } catch (Exception e) {
            LOGGER.error("createFolder(): Exception {}", e);
        }
        return null;
    }

    @Override
    public void deleteObjectOrCancelCheckOut(String repositoryId, String objectId, Boolean allVersions,
                                             ExtensionsData extension) {
        LOGGER.info("deleteObjectOrCancelCheckOut()");
        getRepository().deleteObject(getCallContext(), objectId);
    }

    @Override
    public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
                                         UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {
        LOGGER.info("deleteTree()");
        return getRepository().deleteTree(getCallContext(), folderId, continueOnFailure);
    }

    @Override
    public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {
        LOGGER.info("entering getAllowableActions()");
        return getRepository().getAllowableActions(getCallContext(), objectId);
    }

    @Override
    public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
                                          BigInteger length, ExtensionsData extension) {
        try {
            LOGGER.info("entering getContentStream()");
            return getRepository().getContentStream(getCallContext(), objectId, offset, length);
        } catch (Exception e) {
            LOGGER.error("getContentStream(): Exception {}", e);
        }
        return null;
    }

    @Override
    public ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
                                IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
                                Boolean includeAcl, ExtensionsData extension) {
        LOGGER.info("entering getObject()");
        return getRepository().getObject(getCallContext(), objectId, null, filter, includeAllowableActions, includeAcl,
                this);
    }

    @Override
    public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
                                      IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
                                      Boolean includeAcl, ExtensionsData extension) {
        LOGGER.info("entering getObjectByPath()");
        return getRepository().getObjectByPath(getCallContext(), path, filter, includeAllowableActions, includeAcl,
                this);
    }

    @Override
    public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {
        LOGGER.info("entering getProperties()");
        ObjectData object = getRepository().getObject(getCallContext(), objectId, null, filter, false, false, this);
        return object.getProperties();
    }

    @Override
    public List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter,
                                             BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        LOGGER.info("entering getRenditions()");
        return Collections.emptyList();
    }

    @Override
    public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
                           ExtensionsData extension) {
        LOGGER.info("moveObject()");
        getRepository().moveObject(getCallContext(), objectId, targetFolderId, this);
    }

    @Override
    public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
                                 Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {
        LOGGER.info("entering setContentStream()");
        try {
            getRepository().changeContentStream(getCallContext(), objectId, overwriteFlag, contentStream, false);
        } catch (Exception e) {
            LOGGER.error("setContentStream(): Exception {}", e);
        }
        LOGGER.info("leaving setContentStream()");
    }

    @Override
    public void appendContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
                                    ContentStream contentStream, boolean isLastChunk, ExtensionsData extension) {
        LOGGER.info("entering appendContentStream()");
        try {
            getRepository().changeContentStream(getCallContext(), objectId, true, contentStream, true);
        } catch (Exception e) {
            LOGGER.error("appendContentStream(): Exception {}", e);
        }
        LOGGER.info("leaving appendContentStream()");
    }

    @Override
    public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
                                    ExtensionsData extension) {
        LOGGER.info("entering deleteContentStream()");
//        todo implement deleting files
//        try  {
//            getRepository().changeContentStream(getCallContext(), objectId, true, null, false);
//        } catch (Exception e) {
//            LOGGER.error("deleteContentStream(): Exception {}", e);
//        }
        LOGGER.info("leaving deleteContentStream()");
    }

    @Override
    public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
                                 Properties properties, ExtensionsData extension) {
        LOGGER.info("entering updateProperties()");
        getRepository().updateProperties(getCallContext(), objectId, properties, this);
        LOGGER.info("entering updateProperties()");
    }

    @Override
    public List<BulkUpdateObjectIdAndChangeToken> bulkUpdateProperties(String repositoryId,
                                                                       List<BulkUpdateObjectIdAndChangeToken> objectIdAndChangeToken, Properties properties,
                                                                       List<String> addSecondaryTypeIds, List<String> removeSecondaryTypeIds, ExtensionsData extension) {
        LOGGER.info("entering bulkUpdateProperties()");
        return getRepository().bulkUpdateProperties(getCallContext(), objectIdAndChangeToken, properties, this);
    }

    // --- versioning service ---

    @Override
    public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter,
                                           Boolean includeAllowableActions, ExtensionsData extension) {
        ObjectData theVersion = getRepository().getObject(getCallContext(), objectId, versionSeriesId, filter,
                includeAllowableActions, false, this);
        LOGGER.info("entering getAllVersions()");
        return Collections.singletonList(theVersion);
    }

    @Override
    public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
                                               Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
                                               String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
        LOGGER.info("entering getObjectOfLatestVersion()");
        return getRepository().getObject(getCallContext(), objectId, versionSeriesId, filter, includeAllowableActions,
                includeAcl, this);
    }

    @Override
    public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
                                                   Boolean major, String filter, ExtensionsData extension) {
        LOGGER.info("entering getPropertiesOfLatestVersion()");
        ObjectData object = getRepository().getObject(getCallContext(), objectId, versionSeriesId, filter, false,
                false, null);

        return object.getProperties();
    }

    // --- ACL service ---

    @Override
    public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {
        LOGGER.info("entering getAcl()");
        return getRepository().getAcl(getCallContext(), objectId);
    }

    // --- discovery service ---

    @Override
    public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions,
                            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
                            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        LOGGER.info("entering query()");
        try {
            return getRepository().query(getCallContext(), statement, includeAllowableActions, maxItems, skipCount, this);
        } catch (IOException e) {
            LOGGER.error("query(): Exception {}", e);
        }
        return null;
    }

}