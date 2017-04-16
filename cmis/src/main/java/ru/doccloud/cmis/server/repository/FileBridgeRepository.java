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
package ru.doccloud.cmis.server.repository;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.*;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.*;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.doccloud.cmis.server.FileBridgeTypeManager;
import ru.doccloud.cmis.server.util.FileBridgeUtils;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.common.util.JsonNodeParser;
import ru.doccloud.common.util.VersionHelper;
import ru.doccloud.document.dto.DocumentDTO;
import ru.doccloud.document.dto.LinkDTO;
import ru.doccloud.document.service.DocumentCrudService;
import ru.doccloud.document.service.FileActionsService;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import static ru.doccloud.cmis.server.util.FileBridgeUtils.*;

/**
 * Implements all repository operations.
 */
public class FileBridgeRepository extends AbstractBridgeRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBridgeRepository.class);

    private FileActionsService fileActionsService;

    public FileBridgeRepository(final String repositoryId, final String rootPath,
                                final FileBridgeTypeManager typeManager, DSLContext jooq, DocumentCrudService crudService, FileActionsService fileActionsService) {
        super(repositoryId, rootPath, crudService);

        LOGGER.trace("FileBridgeRepository(repositoryId={}, rootPath={}, typeManager={}, jooq={}, crudService= {}, fileActionsService={})",repositoryId, rootPath, typeManager, jooq, crudService, fileActionsService);

        // set type manager objects
        this.typeManager = typeManager;

        // set up read-write user map
        readWriteUserMap = new ConcurrentHashMap<>();

        this.fileActionsService = fileActionsService;
    }

    /**
     * Sets read-write flag for the given user.
     */
    public void setUserReadWrite(final String user) {
        LOGGER.trace("setUserReadWrite(user={})", user);
        if (StringUtils.isBlank(user)) {
            return;
        }

        readWriteUserMap.put(user, false);
    }

    // --- CMIS operations ---



    /**
     * CMIS getTypesChildren.
     */
    public TypeDefinitionList getTypeChildren(CallContext context, String typeId, Boolean includePropertyDefinitions,
                                              BigInteger maxItems, BigInteger skipCount) {
        checkUser(context, false);

        return typeManager.getTypeChildren(context, typeId, includePropertyDefinitions, maxItems, skipCount);
    }

    /**
     * CMIS getTypesDescendants.
     */
    public List<TypeDefinitionContainer> getTypeDescendants(CallContext context, String typeId, BigInteger depth,
                                                            Boolean includePropertyDefinitions) {
        checkUser(context, false);

        return typeManager.getTypeDescendants(context, typeId, depth, includePropertyDefinitions);
    }

    /**
     * CMIS getTypeDefinition.
     */
    public TypeDefinition getTypeDefinition(CallContext context, String typeId) {
        checkUser(context, false);

        return typeManager.getTypeDefinition(context, typeId);
    }

    /**
     * Create* dispatch for AtomPub.
     */
    public ObjectData create(CallContext context, Properties properties, String folderId, ContentStream contentStream,
                             VersioningState versioningState, ObjectInfoHandler objectInfos) throws Exception {
        LOGGER.debug("entering create(context={}, properties = {}, folderId={}, versionState={}, objectInfos={})", context, properties, folderId, versioningState, objectInfos);
        boolean userReadOnly = checkUser(context, true);

        final String typeId = getObjectTypeId(properties);
        LOGGER.debug("create(): typeId {}", typeId);
        if (StringUtils.isBlank(typeId)) {
            throw new CmisInvalidArgumentException("Type Id is not set!");
        }
        TypeDefinition type = getTypeDefinitionByTypeId(typeId);
        LOGGER.debug("create(): TypeDefinition {}", type);
        DocumentDTO documentDTO;
        if (type.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT) {
            documentDTO = createDocument(context, properties, folderId, contentStream, versioningState, type);
        } else if (type.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
            documentDTO = createFolder(context, properties, folderId, type);
        } else {
            throw new CmisObjectNotFoundException("Cannot create object of type '" + typeId + "'!");
        }

        LOGGER.debug("leaving create(): document with id has been created {}", documentDTO);
        return compileObjectData(context, documentDTO, null, false, false, userReadOnly, objectInfos);
    }

    /**
     * CMIS createDocument.
     */
    private DocumentDTO createDocument(CallContext context, Properties properties, String folderId,
                                 ContentStream contentStream, VersioningState versioningState, TypeDefinition type) throws Exception {
        LOGGER.debug("entering createDocument(context={}, properties = {}, folderId={}, versionState={}, type= {})", context, properties, folderId, versioningState, type);
        checkUser(context, true);

        // check versioning state
        //if (VersioningState.NONE != versioningState) {
        //    throw new CmisConstraintException("Versioning not supported!");
        //}

        // get parent
        DocumentDTO parent = getDocument(folderId);
        LOGGER.debug("createDocument(): parent is {}", parent);
        if (!isFolder(parent.getType())) {
            throw new CmisObjectNotFoundException("Parent is not a folder!");
        }
        DocumentDTO doc = null;
        try {
            checkNewProperties(properties, BaseTypeId.CMIS_DOCUMENT, type);

            final String name = FileBridgeUtils.getStringProperty(properties, PropertyIds.NAME);

            LOGGER.debug("createDocument(): name is {}", name);

            doc = new DocumentDTO(name, "document", context.getUsername());
            doc.setDocVersion(VersionHelper.generateMinorDocVersion(doc.getDocVersion()));
            doc = crudService.add(doc, context.getUsername());

            LOGGER.debug("createDocument(): Document has been created {}", doc);
            crudService.addToFolder(doc, parent.getId());


            // write content, if available
            if (contentStream != null && contentStream.getStream() != null) {

                final String filePath = writeContent(doc, contentStream.getStream());
                LOGGER.debug("createDocument(): content was written filePath {}", filePath);
                if (!StringUtils.isBlank(filePath)) {
                    BigInteger fileLength = contentStream.getBigLength();
                    String mimeType = contentStream.getMimeType();
                    String fileName = contentStream.getFileName();
                    LOGGER.debug("createDocument(): Uploaded file - {} - {} - {} - {}",filePath, fileLength, mimeType, fileName);
                    if (fileLength == null) {
                        doc.setFileLength(0L);
                    } else {
                        doc.setFileLength(fileLength.longValue());
                    }
                    doc.setFilePath(filePath);
                    doc.setFileMimeType(mimeType);
                    doc.setModifier(context.getUsername());
                    doc.setFileName(fileName);
                    crudService.updateFileInfo(doc);
                }
            }

            LOGGER.debug("leaving createDocument(): created document {}", doc);
            return doc;
        } catch (Exception e){
            if(doc != null && doc.getId() != null) {
                crudService.deleteLink(parent.getId(), doc.getId());
                crudService.delete(doc.getId());
            }
            throw new Exception(e.getMessage());
        }
    }

    /**
     * CMIS createDocumentFromSource.
     */
    public String createDocumentFromSource(CallContext context, String sourceId, Properties properties,
                                           String folderId, VersioningState versioningState) throws Exception {
        LOGGER.debug("entering createDocumentFromSource(context={}, sourceId= {}, properties = {}, folderId={}, versionState={})",
                context, sourceId, properties, folderId, versioningState);
        checkUser(context, true);

        // check versioning state
//        if (VersioningState.NONE != versioningState) {
//            throw new CmisConstraintException("Versioning not supported!");
//        }

        // get parent
        DocumentDTO parent = getDocument(folderId);

        LOGGER.debug("createDocumentFromSource(): parent document is {}", parent);
        DocumentDTO doc = null;
        if (!isFolder(parent.getType())) {
            throw new CmisObjectNotFoundException("Parent is not a folder!");
        }
        try {
            // get source
            DocumentDTO source = getDocument(sourceId);
            LOGGER.debug("createDocumentFromSource(): source document is {}", source);

            // check properties
            final String typeId = getObjectTypeId(properties);

            LOGGER.debug("createDocumentFromSource(): typeId is {}", typeId);
            if (StringUtils.isBlank(typeId)) {
                throw new CmisInvalidArgumentException("Type Id is not set!");
            }
            TypeDefinition type = getTypeDefinitionByTypeId(typeId);
            checkCopyProperties(properties, BaseTypeId.CMIS_DOCUMENT.value(), type);

            // check the name
            String name = null;
            if (properties != null && properties.getProperties() != null) {
                name = FileBridgeUtils.getStringProperty(properties, PropertyIds.NAME);
            }
            if (name == null) {
                name = source.getTitle();
            }

            doc = crudService.add(new DocumentDTO(name, "document", context.getUsername()), context.getUsername());

            LOGGER.debug("createDocumentFromSource(): Document has been created {}", doc);
            crudService.addToFolder(doc, parent.getId());

            // copy content

            String filePath = writeContent(doc, new FileInputStream(source.getFilePath()));
            LOGGER.debug("createDocumentFromSource(): content was written filePath {}", filePath);
            if(filePath != null) {
                doc.setFilePath(filePath);
                doc.setFileMimeType(source.getFileMimeType());
                doc.setFileLength(source.getFileLength());
                doc.setFileName(source.getFileName());
                doc.setModifier(context.getUsername());
                doc = crudService.update(doc, context.getUsername());
            }

            return getId(doc.getId());

        } catch (Exception e){
            if(doc != null && doc.getId() != null) {
                crudService.deleteLink(parent.getId(), doc.getId());
                crudService.delete(doc.getId());
            }
            LOGGER.error("createDocumentFromSource(): Exception {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    /**
     * CMIS createFolder.
     */
    private DocumentDTO createFolder(CallContext context, Properties properties, String folderId, TypeDefinition type) throws Exception {
        checkUser(context, true);
        LOGGER.debug("entering createFolder(context={}, properties = {}, folderId={}, type={})", context, properties, folderId, type);
        // get parent
        DocumentDTO parent = getDocument(folderId);
        LOGGER.debug("createFolder(): parent is {}", parent);

        // check properties
        checkNewProperties(properties, BaseTypeId.CMIS_FOLDER, type);
        if (!isFolder(parent.getType())) {
            throw new CmisObjectNotFoundException("Parent is not a folder!");
        }

        // create the folder
        String name = FileBridgeUtils.getStringProperty(properties, PropertyIds.NAME);

        LOGGER.debug("createFolder(): name is {}", name);
        DocumentDTO doc = null;
        try {
            doc = new DocumentDTO(name, "folder", context.getUsername());
            doc.setDocVersion(VersionHelper.generateMinorDocVersion(doc.getDocVersion()));
            doc = crudService.add(doc, context.getUsername());
            crudService.addToFolder(doc, parent.getId());

            LOGGER.debug("leaving createFolder(): created folder {}", doc);
            return doc;
        } catch (Exception e){
            if(doc != null && doc.getId() != null) {
                crudService.deleteLink(parent.getId(), doc.getId());
                crudService.delete(doc.getId());
            }
            throw new Exception(e.getMessage());
        }
    }


    /**
     * CMIS moveObject.
     */
    public ObjectData moveObject(CallContext context, Holder<String> objectId, String targetFolderId,
                                 ObjectInfoHandler objectInfos) {
        LOGGER.debug("entering moveObject(context={}, objectId={}, targetFolderId={})", context, objectId, targetFolderId);
        boolean userReadOnly = checkUser(context, true);

        if (objectId == null) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        // get the file and parent
        DocumentDTO doc = getDocument(objectId.getValue());

        LOGGER.debug(" moveObject(): document for move {}", doc);

        DocumentDTO parent = getFirstParent(doc.getId());

        LOGGER.debug(" moveObject(): parent document {}", parent);

        if (parent!=null){

            LinkDTO deletedLink = crudService.deleteLink(parent.getId(), doc.getId());
            LOGGER.debug("moveObject(): existing link {} has been deleted", deletedLink);
        }
        LinkDTO link = crudService.addLink(Long.parseLong(targetFolderId), doc.getId());

        LOGGER.debug("leaving moveObject(): new link {} has been created for object {}", link, doc);

        return compileObjectData(context, doc, null, false, false, userReadOnly, objectInfos);
    }

    /**
     * CMIS setContentStream, deleteContentStream, and appendContentStream.
     */
    public void changeContentStream(CallContext context, Holder<String> objectId, Boolean overwriteFlag,
                                    ContentStream contentStream, boolean append) {
        checkUser(context, true);

        if (objectId == null) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        // get the file
        File file = getFile(objectId.getValue());
        if (!file.isFile()) {
            throw new CmisStreamNotSupportedException("Not a file!");
        }

        // check overwrite
        boolean owf = FileBridgeUtils.getBooleanParameter(overwriteFlag, true);
        if (!owf && file.length() > 0) {
            throw new CmisContentAlreadyExistsException("Content already exists!");
        }

        OutputStream out = null;
        InputStream in = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file, append), BUFFER_SIZE);

            if (contentStream == null || contentStream.getStream() == null) {
                // delete content
                out.write(new byte[0]);
            } else {
                // set content
                in = new BufferedInputStream(contentStream.getStream(), BUFFER_SIZE);

                byte[] buffer = new byte[BUFFER_SIZE];
                int b;
                while ((b = in.read(buffer)) > -1) {
                    out.write(buffer, 0, b);
                }
            }
        } catch (Exception e) {
            throw new CmisStorageException("Could not write content: " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * CMIS deleteObject.
     */
    public void deleteObject(CallContext context, String objectId) {

        LOGGER.debug("entering deleteObject(context={}, objectId = {})", context, objectId);
        checkUser(context, true);

        DocumentDTO doc = getDocument(objectId);

        LOGGER.debug("deleteObject(): document for deleting {}", doc);
        // check if it is a folder and if it is empty
        if (isFolder(doc.getType())) {
            if (crudService.findAllByParent(doc.getId()).size() > 0) {
                throw new CmisConstraintException("Folder is not empty!");
            }
        }

        LinkDTO deletedLink = null;
        try {

            DocumentDTO parent = getFirstParent(doc.getId());

            LOGGER.debug("deleteObject(): parent document {}", parent);

            if (parent != null) {
                deletedLink = crudService.deleteLink(parent.getId(), doc.getId());
                LOGGER.debug("deleteObject(): link has been deleted {}", deletedLink);
            }

            // delete doc
            DocumentDTO deletedDto = crudService.delete(doc.getId());
            LOGGER.debug("leaving deleteObject(): document {} has been deleted successfully", deletedDto);
        } catch (Exception e){
            LOGGER.error("deleteObject(): exception {} ", e.getMessage());
            if(deletedLink != null)
                crudService.addLink(deletedLink.getHead_id(), deletedLink.getTail_id());
        }
    }

    /**
     * CMIS deleteTree.
     */
    public FailedToDeleteData deleteTree(CallContext context, String folderId, Boolean continueOnFailure) {
        LOGGER.debug("entering deleteTree(context={}, folderId = {}, continueOnFailure={})", context, folderId, continueOnFailure);
        checkUser(context, true);

        boolean cof = FileBridgeUtils.getBooleanParameter(continueOnFailure, false);


        // get the doc
        DocumentDTO doc = getDocument(folderId);

        LOGGER.debug("deleteTree(): document for delete {}", doc);

        FailedToDeleteDataImpl result = new FailedToDeleteDataImpl();
        result.setIds(new ArrayList<>());

        // if it is a folder, remove it recursively
        if (isFolder(doc.getType())) {
            deleteFolder(doc, cof, result);
        } else {
            throw new CmisConstraintException("Object is not a folder!");
        }

        LOGGER.debug("leaving deleteTree() : result {}", result);
        return result;
    }


    /**
     * Writes the content to disc.
     */
    private String writeContent(DocumentDTO doc, InputStream stream) throws Exception {
        try {

            DocumentDTO settings = crudService.findSettings();
            JsonNode settingsNode = settings.getData();

            final String filePath = fileActionsService.writeFile(JsonNodeParser.getValueJsonNode(settingsNode, "repository"),  doc.getUuid(), org.apache.commons.io.IOUtils.toByteArray(stream));
            LOGGER.debug("writeContent(): File has been saved on the disc, path to file {}", filePath);
            doc.setFilePath(filePath);

            return filePath;
        } catch (IOException e) {

            throw new CmisStorageException("Could not write content: " + e.getMessage(), e);
        }

    }

    /**
     * Removes a folder and its content.
     */
    private boolean deleteFolder(DocumentDTO doc, boolean continueOnFailure, FailedToDeleteDataImpl ftd) {
        LOGGER.debug("entering deleteFolder(doc={}, continueOnFailure={}, ftd={})", doc, continueOnFailure, ftd);
        boolean success = true;

        List<DocumentDTO> docList = crudService.findAllByParent(doc.getId());
        for (DocumentDTO childDoc : docList) {
            LOGGER.debug(" deleteFolder(): childDoc {} : ", childDoc);
            if (isFolder(childDoc.getType())) {
                if (!deleteFolder(childDoc, continueOnFailure, ftd)) {
                    if (!continueOnFailure) {
                        return false;
                    }
                    success = false;
                }
            } else {
                LinkDTO deletedChildLink = null;

                DocumentDTO parent = getFirstParent(doc.getId());

                LOGGER.debug("deleteFolder(): parent document {}", parent);

                if (parent != null) {
                    deletedChildLink = crudService.deleteLink(parent.getId(), doc.getId());
                    LOGGER.debug("deleteFolder(): childlink has been deleted {}", deletedChildLink);
                }
                DocumentDTO childDeleted = crudService.delete(childDoc.getId());
                LOGGER.debug(" deleteFolder(): childDeleted {} : ", childDeleted);
                if (childDeleted == null) {
                    ftd.getIds().add(getId(childDoc.getId()));
                    if(deletedChildLink != null)
                        crudService.addLink(deletedChildLink.getHead_id(), deletedChildLink.getTail_id());
                    if (!continueOnFailure) {
                        return false;
                    }
                    success = false;
                }
            }
        }

        LinkDTO deletedLink = null;
        DocumentDTO parent = getFirstParent(doc.getId());

        LOGGER.debug("deleteFolder(): parent document for deleted folder {}", parent);

        if (parent != null) {
            deletedLink = crudService.deleteLink(parent.getId(), doc.getId());
            LOGGER.debug("deleteFolder(): link has been deleted {}", deletedLink);
        }
        DocumentDTO deleted = crudService.delete(doc.getId());
        LOGGER.debug(" deleteFolder(): deleted object {} : ", deleted);
        if (deleted==null) {
            ftd.getIds().add(getId(doc.getId()));
            if(deletedLink != null)
                crudService.addLink(deletedLink.getHead_id(), deletedLink.getTail_id());
            success = false;
        }

        return success;
    }

    /**
     * Removes a folder and its content.
     */
    private boolean deleteFolder(File folder, boolean continueOnFailure, FailedToDeleteDataImpl ftd) {
        if(folder == null){
            LOGGER.warn("deleteFolder(): Folder is null");
            return false;
        }

        boolean success = true;

        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                if (!deleteFolder(file, continueOnFailure, ftd)) {
                    if (!continueOnFailure) {
                        return false;
                    }
                    success = false;
                }
            } else {
                if (!file.delete()) {
                    ftd.getIds().add(getId(file));
                    if (!continueOnFailure) {
                        return false;
                    }
                    success = false;
                }
            }
        }

        if (!folder.delete()) {
            ftd.getIds().add(getId(folder));
            success = false;
        }

        return success;
    }

    /**
     * CMIS updateProperties.
     */
    public ObjectData updateProperties(CallContext context, Holder<String> objectId, Properties properties,
                                       ObjectInfoHandler objectInfos) {
        boolean userReadOnly = checkUser(context, true);

        // check object id
        if (objectId == null || objectId.getValue() == null) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        // get the file or folder
        DocumentDTO doc = getDocument(objectId.getValue());

        // check the properties
        String typeId = (isFolder(doc.getType()) ? BaseTypeId.CMIS_FOLDER.value() : BaseTypeId.CMIS_DOCUMENT.value());

        if (StringUtils.isBlank(typeId)) {
            throw new CmisInvalidArgumentException("Type Id is not set!");
        }
        TypeDefinition type = getTypeDefinitionByTypeId(typeId);
        checkCopyProperties(properties, BaseTypeId.CMIS_DOCUMENT.value(), type);
        checkUpdateProperties(properties, typeId, type);

        // get and check the new name
        String newName = FileBridgeUtils.getStringProperty(properties, PropertyIds.NAME);
        boolean isRename = (newName != null) && (!doc.getTitle().equals(newName));
        if (isRename && !isValidName(newName)) {
            throw new CmisNameConstraintViolationException("Name is not valid!");
        }

        if (isRename) {
            doc.setTitle(newName);
            crudService.update(doc ,context.getUsername());
        }

        return compileObjectData(context, doc, null, false, false, userReadOnly, objectInfos);
    }

    /**
     * CMIS bulkUpdateProperties.
     */
    public List<BulkUpdateObjectIdAndChangeToken> bulkUpdateProperties(CallContext context,
                                                                       List<BulkUpdateObjectIdAndChangeToken> objectIdAndChangeToken, Properties properties,
                                                                       ObjectInfoHandler objectInfos) {
        checkUser(context, true);

        if (objectIdAndChangeToken == null) {
            throw new CmisInvalidArgumentException("No object ids provided!");
        }

        List<BulkUpdateObjectIdAndChangeToken> result = new ArrayList<BulkUpdateObjectIdAndChangeToken>();

        for (BulkUpdateObjectIdAndChangeToken oid : objectIdAndChangeToken) {
            if (oid == null) {
                // ignore invalid ids
                continue;
            }
            try {
                Holder<String> oidHolder = new Holder<String>(oid.getId());
                updateProperties(context, oidHolder, properties, objectInfos);

                result.add(new BulkUpdateObjectIdAndChangeTokenImpl(oid.getId(), oidHolder.getValue(), null));
            } catch (CmisBaseException e) {
                // ignore exceptions - see specification
            }
        }

        return result;
    }

    /**
     * CMIS getObject.
     */
    public ObjectData getObject(CallContext context, String objectId, String versionServicesId, String filter,
                                Boolean includeAllowableActions, Boolean includeAcl, ObjectInfoHandler objectInfos) {
        LOGGER.debug("entering getObject(objectId={}, versionServicesId = {}, filter= {}, includeAllowableActions={}, includeACL={}, objectInfos={})", objectId, versionServicesId,
                filter, includeAllowableActions, includeAcl, objectInfos);
        boolean userReadOnly = checkUser(context, false);


        // check id
        if (objectId == null && versionServicesId == null) {
            throw new CmisInvalidArgumentException("Object Id must be set.");
        }

        if (objectId == null) {
            // this works only because there are no versions in a file system
            // and the object id and version series id are the same
            objectId = versionServicesId;
        }
        DocumentDTO doc = getDocument(objectId);

        // set defaults if values not set
        boolean iaa = FileBridgeUtils.getBooleanParameter(includeAllowableActions, false);
        boolean iacl = FileBridgeUtils.getBooleanParameter(includeAcl, false);

        // split filter
        Set<String> filterCollection = FileBridgeUtils.splitFilter(filter);

        LOGGER.debug("leaving getObject(): found object {}", doc);
        // gather properties
        return compileObjectData(context, doc, filterCollection, iaa, iacl, userReadOnly, objectInfos);
    }

    /**
     * CMIS getAllowableActions.
     */
    public AllowableActions getAllowableActions(CallContext context, String objectId) {
        boolean userReadOnly = checkUser(context, false);

        // get the file or folder
        File file = getFile(objectId);
        if (!file.exists()) {
            throw new CmisObjectNotFoundException("Object not found!");
        }

        return compileAllowableActions(file, userReadOnly);
    }

    /**
     * CMIS getACL.
     */
    public Acl getAcl(CallContext context, String objectId) {
        checkUser(context, false);

        // get the file or folder
        File file = getFile(objectId);
        if (!file.exists()) {
            throw new CmisObjectNotFoundException("Object not found!");
        }

        return compileAcl(file);
    }

    /**
     * CMIS getContentStream.
     */
    public ContentStream getContentStream(CallContext context, String objectId, BigInteger offset, BigInteger length) throws Exception {
        checkUser(context, false);

        // get the file
        final DocumentDTO doc = getDocument(objectId);


        if (StringUtils.isBlank(doc.getFilePath())) {
            throw new CmisConstraintException("Document has no content!");
        }

        byte[] contentByteArr = fileActionsService.readFile(doc.getFilePath());

        // compile data
        ContentStreamImpl result;
        if ((offset != null && offset.longValue() > 0) || length != null) {
            result = new PartialContentStreamImpl();
        } else {
            result = new ContentStreamImpl();
        }

        result.setFileName(doc.getFileName());
        result.setLength(BigInteger.valueOf(doc.getFileLength()));
        result.setMimeType(MimeTypes.getMIMEType(doc.getFileMimeType()));
        result.setStream(new ByteArrayInputStream(contentByteArr));


        return result;
    }

    /**
     * CMIS getChildren.
     */
    public ObjectInFolderList getChildren(CallContext context, String objectId, String filter,
                                          Boolean includeAllowableActions, Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount,
                                          ObjectInfoHandler objectInfos) {
        boolean userReadOnly = checkUser(context, false);

        // split filter
        Set<String> filterCollection = FileBridgeUtils.splitFilter(filter);

        // set defaults if values not set
        boolean iaa = FileBridgeUtils.getBooleanParameter(includeAllowableActions, false);
        boolean ips = FileBridgeUtils.getBooleanParameter(includePathSegment, false);

        LOGGER.debug("Folder ID: {}", objectId);
        Long parent = Long.parseLong(objectId);
        List<DocumentDTO> docList = crudService.findAllByParent(parent);

        LOGGER.debug("Found {} Document entries.", docList != null ? docList.size() : null);


        // skip and max
        int skip = (skipCount == null ? 0 : skipCount.intValue());
        if (skip < 0) {
            skip = 0;
        }

        int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
        if (max < 0) {
            max = Integer.MAX_VALUE;
        }

        DocumentDTO curdoc = getDocument(objectId);

        if (context.isObjectInfoRequired()) {
            compileObjectData(context, curdoc, null, false, false, userReadOnly, objectInfos);
        }

        // prepare result
        ObjectInFolderListImpl result = new ObjectInFolderListImpl();
        result.setObjects(new ArrayList<ObjectInFolderData>());
        result.setHasMoreItems(false);
        int count = 0;

        for (DocumentDTO doc : docList){
            count++;

            if (skip > 0) {
                skip--;
                continue;
            }

            if (result.getObjects().size() >= max) {
                result.setHasMoreItems(true);
                continue;
            }

            // build and add child object
            ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
            objectInFolder.setObject(compileObjectData(context, doc, filterCollection, iaa, false, userReadOnly,
                    objectInfos));
            if (ips) {
                objectInFolder.setPathSegment(doc.getTitle());
            }

            result.getObjects().add(objectInFolder);
        }


        result.setNumItems(BigInteger.valueOf(count));

        return result;
    }

    /**
     * CMIS getDescendants.
     */
    public List<ObjectInFolderContainer> getDescendants(CallContext context, String folderId, BigInteger depth,
                                                        String filter, Boolean includeAllowableActions, Boolean includePathSegment, ObjectInfoHandler objectInfos,
                                                        boolean foldersOnly) {
        boolean userReadOnly = checkUser(context, false);

        // check depth
        int d = (depth == null ? 2 : depth.intValue());
        if (d == 0) {
            throw new CmisInvalidArgumentException("Depth must not be 0!");
        }
        if (d < -1) {
            d = -1;
        }

        // split filter
        Set<String> filterCollection = FileBridgeUtils.splitFilter(filter);

        // set defaults if values not set
        boolean iaa = FileBridgeUtils.getBooleanParameter(includeAllowableActions, false);
        boolean ips = FileBridgeUtils.getBooleanParameter(includePathSegment, false);

        // get the folder
        File folder = getFile(folderId);
        if (!folder.isDirectory()) {
            throw new CmisObjectNotFoundException("Not a folder!");
        }

        // set object info of the the folder
        if (context.isObjectInfoRequired()) {
            compileObjectData(context, folder, null, false, false, userReadOnly, objectInfos);
        }

        // get the tree
        List<ObjectInFolderContainer> result = new ArrayList<ObjectInFolderContainer>();
        gatherDescendants(context, folder, result, foldersOnly, d, filterCollection, iaa, ips, userReadOnly,
                objectInfos);

        return result;
    }

    /**
     * CMIS getFolderParent.
     */
    public ObjectData getFolderParent(CallContext context, String folderId, String filter, ObjectInfoHandler objectInfos) {
        List<ObjectParentData> parents = getObjectParents(context, folderId, filter, false, false, objectInfos);

        if (parents.isEmpty()) {
            throw new CmisInvalidArgumentException("The root folder has no parent!");
        }

        return parents.get(0).getObject();
    }

    /**
     * CMIS getObjectParents.
     */
    public List<ObjectParentData> getObjectParents(CallContext context, String objectId, String filter,
                                                   Boolean includeAllowableActions, Boolean includeRelativePathSegment, ObjectInfoHandler objectInfos) {
        boolean userReadOnly = checkUser(context, false);

        // split filter
        Set<String> filterCollection = FileBridgeUtils.splitFilter(filter);

        // set defaults if values not set
        boolean iaa = FileBridgeUtils.getBooleanParameter(includeAllowableActions, false);
        boolean irps = FileBridgeUtils.getBooleanParameter(includeRelativePathSegment, false);

        // get the file or folder
        DocumentDTO doc = getDocument(objectId);

        // don't climb above the root folder
        if (doc.getId()==0) {
            return Collections.emptyList();
        }

        // set object info of the the object
        if (context.isObjectInfoRequired()) {
            compileObjectData(context, doc, null, false, false, userReadOnly, objectInfos);
        }

        // get parent folder
        DocumentDTO parent = getFirstParent(doc.getId());
        ObjectData object = compileObjectData(context, parent, filterCollection, iaa, false, userReadOnly, objectInfos);

        ObjectParentDataImpl result = new ObjectParentDataImpl();
        result.setObject(object);
        if (irps) {
            result.setRelativePathSegment(doc.getTitle());
        }

        return Collections.<ObjectParentData> singletonList(result);
    }

    /**
     * CMIS getObjectByPath.
     */
    public ObjectData getObjectByPath(CallContext context, String folderPath, String filter,
                                      boolean includeAllowableActions, boolean includeACL, ObjectInfoHandler objectInfos) {
        boolean userReadOnly = checkUser(context, false);

        // split filter
        Set<String> filterCollection = FileBridgeUtils.splitFilter(filter);

        // check path
        if (folderPath == null || folderPath.length() == 0) {
            throw new CmisInvalidArgumentException("Invalid folder path!");
        }

        Pageable pageable = new PageRequest(0, 1);
        List<DocumentDTO> dtos = crudService.findBySearchTerm(folderPath.substring(folderPath.lastIndexOf("/")+1, folderPath.length()), pageable);
        DocumentDTO doc = dtos.iterator().next();
        return compileObjectData(context, doc, filterCollection, includeAllowableActions, includeACL, userReadOnly,
                objectInfos);
    }

    /**
     * CMIS query (simple IN_FOLDER queries only)
     */
    public ObjectList query(CallContext context, String statement, Boolean includeAllowableActions,
                            BigInteger maxItems, BigInteger skipCount, ObjectInfoHandler objectInfos) {
        boolean userReadOnly = checkUser(context, false);

        Matcher matcher = IN_FOLDER_QUERY_PATTERN.matcher(statement.trim());

        if (!matcher.matches()) {
            throw new CmisInvalidArgumentException("Invalid or unsupported query.");
        }

        String typeId = matcher.group(1);
        String folderId = matcher.group(2);

        TypeDefinition type = getTypeDefinitionByTypeId(typeId);

        boolean queryFiles = (type.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT);

        if (folderId.length() == 0) {
            throw new CmisInvalidArgumentException("Invalid folder id.");
        }

        // set defaults if values not set
        boolean iaa = FileBridgeUtils.getBooleanParameter(includeAllowableActions, false);

        // skip and max
        int skip = (skipCount == null ? 0 : skipCount.intValue());
        if (skip < 0) {
            skip = 0;
        }

        int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
        if (max < 0) {
            max = Integer.MAX_VALUE;
        }

        // get the folder
        File folder = getFile(folderId);
        if (!folder.isDirectory()) {
            throw new CmisInvalidArgumentException("Not a folder!");
        }

        // prepare result
        ObjectListImpl result = new ObjectListImpl();
        result.setObjects(new ArrayList<ObjectData>());
        result.setHasMoreItems(false);
        int count = 0;

        // iterate through children
        for (File hit : folder.listFiles()) {
            // skip hidden files
            if (hit.isHidden()) {
                continue;
            }

            // skip directory if documents are requested
            if (hit.isDirectory() && queryFiles) {
                continue;
            }

            // skip files if folders are requested
            if (hit.isFile() && !queryFiles) {
                continue;
            }

            count++;

            if (skip > 0) {
                skip--;
                continue;
            }

            if (result.getObjects().size() >= max) {
                result.setHasMoreItems(true);
                continue;
            }

            // build and add child object
            ObjectData object = compileObjectData(context, hit, null, iaa, false, userReadOnly, objectInfos);

            // set query names
            for (PropertyData<?> prop : object.getProperties().getPropertyList()) {
                ((MutablePropertyData<?>) prop).setQueryName(type.getPropertyDefinitions().get(prop.getId())
                        .getQueryName());
            }

            result.getObjects().add(object);
        }

        result.setNumItems(BigInteger.valueOf(count));

        return result;
    }

    /**
     * Sets read-only flag for the given user.
     */
    public void setUserReadOnly(final String user) {
        LOGGER.trace("setUserReadOnly(user={})", user);
        if (StringUtils.isBlank(user)) {
            return;
        }
        readWriteUserMap.put(user, true);
    }

    /**
     * Gather the children of a folder.
     */
    private void gatherDescendants(CallContext context, File folder, List<ObjectInFolderContainer> list,
                                   boolean foldersOnly, int depth, Set<String> filter, boolean includeAllowableActions,
                                   boolean includePathSegments, boolean userReadOnly, ObjectInfoHandler objectInfos) {
        if(folder == null) {
            LOGGER.warn("gatherDescendants(): parent folder is null or it contains no any children");
            return;
        }
        // iterate through children
        for (File child : folder.listFiles()) {
            // skip hidden and shadow files
            if (child.isHidden()) {
                continue;
            }

            // folders only?
            if (foldersOnly && !child.isDirectory()) {
                continue;
            }

            // add to list
            ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
            objectInFolder.setObject(compileObjectData(context, child, filter, includeAllowableActions, false,
                    userReadOnly, objectInfos));
            if (includePathSegments) {
                objectInFolder.setPathSegment(child.getName());
            }

            ObjectInFolderContainerImpl container = new ObjectInFolderContainerImpl();
            container.setObject(objectInFolder);

            list.add(container);

            // move to next level
            if (depth != 1 && child.isDirectory()) {
                container.setChildren(new ArrayList<>());
                gatherDescendants(context, child, container.getChildren(), foldersOnly, depth - 1, filter,
                        includeAllowableActions, includePathSegments, userReadOnly, objectInfos);
            }
        }
    }

    // --- helpers ---


    private DocumentDTO getDocument(String objectId) {
        if(StringUtils.isBlank(objectId))
            throw new IllegalArgumentException("Object id is null");

        DocumentDTO found = crudService.findById(Long.parseLong(objectId));
        if(found == null)
            throw new DocumentNotFoundException("Document was not found in database");

        LOGGER.debug("Found Document entry: {}", found);

        return found;
    }

    @Override
    public String toString() {
        return "FileBridgeRepository{" +
                "repositoryId='" + repositoryId + '\'' +
                ", root=" + root +
                '}';
    }
}