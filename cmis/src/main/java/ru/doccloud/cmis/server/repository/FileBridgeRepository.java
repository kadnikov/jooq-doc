
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
import ru.doccloud.common.util.VersionHelper;
import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.service.document.dto.LinkDTO;
import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.storage.StorageActionsService;
import ru.doccloud.storage.storagesettings.StorageAreaSettings;
import ru.doccloud.storagemanager.StorageManager;
import ru.doccloud.storagemanager.Storages;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import static ru.doccloud.cmis.server.util.FileBridgeUtils.*;

/**
 * Implements all repository operations.
 */
public class FileBridgeRepository extends AbstractFileBridgeRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBridgeRepository.class);

    private final StorageActionsService storageActionsService;

    private final DocumentCrudService crudService;

    private JsonNode settingsNode;
    
    private final StorageManager storageManager;

//    todo make local cache with objectId and appropriate dto object to avoid redundant calls of getDocument method
//    private final Map<String, DocumentDTO> localDocumentDtoCache;

    public FileBridgeRepository(final String repositoryId, final String rootPath,
                                final FileBridgeTypeManager typeManager, DSLContext jooq, DocumentCrudService crudService, StorageAreaSettings storageAreaSettings, StorageManager storageManager) throws Exception {
        super(repositoryId, rootPath, typeManager);

        LOGGER.trace("FileBridgeRepository(repositoryId={}, rootPath={}, typeManager={}, jooq={}, crudService= {}, storageAreaSettings = {}, storageManager={})",repositoryId, rootPath, typeManager, jooq, crudService, storageAreaSettings, storageManager);

        this.storageManager = storageManager;
        settingsNode = (JsonNode) storageAreaSettings.getStorageSetting();

        Storages defaultStorage = storageManager.getDefaultStorage(settingsNode);
        LOGGER.trace("FileBridgeRepository( defaultStorage = {})", defaultStorage);

        storageActionsService = storageManager.getStorageService(defaultStorage);
        this.crudService = crudService;
//        localDocumentDtoCache = new ConcurrentHashMap<>();
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
        DocumentDTO doc;
        if (type.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT) {
            doc = createDocument(context, properties, folderId, contentStream, versioningState, type);
        } else if (type.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
            doc = createFolder(context, properties, folderId, type);
        } else {
            throw new CmisObjectNotFoundException("Cannot create object of type '" + typeId + "'!");
        }

        final DocumentDTO parent = getDocument(doc.getParent());
        LOGGER.debug("create(): parent document {}", parent);

        LOGGER.debug("leaving create(): document with id has been created {}", doc);
        return compileObjectData(context, doc, parent, null, false, false, userReadOnly, objectInfos);
    }

    /**
     * CMIS createDocument.
     */
    private DocumentDTO createDocument(CallContext context, Properties properties, String folderId,
                                 ContentStream contentStream, VersioningState versioningState, TypeDefinition type) throws Exception {
        LOGGER.trace("entering createDocument(context={}, properties = {}, folderId={}, versionState={}, type= {})", context, properties, folderId, versioningState, type);
        checkUser(context, true);

        // check versioning state
        //if (VersioningState.NONE != versioningState) {
        //    throw new CmisConstraintException("Versioning not supported!");
        //}

        // get parent
        final DocumentDTO parent = getDocument(folderId);
        LOGGER.trace("createDocument(): parent is {}", parent);
        if (parent == null || !isFolder(parent.getType())) {
            throw new CmisObjectNotFoundException("Parent is not a folder!");
        }
        DocumentDTO doc = null;
        try {
            checkNewProperties(properties, BaseTypeId.CMIS_DOCUMENT, type);

            final String name = FileBridgeUtils.getStringProperty(properties, PropertyIds.NAME);

            LOGGER.trace("createDocument(): name is {}", name);

            doc = new DocumentDTO(name, "document", context.getUsername());
            doc.setDocVersion(VersionHelper.generateMinorDocVersion(doc.getDocVersion()));
            doc = crudService.add(doc, context.getUsername());

            LOGGER.trace("createDocument(): Document has been created {}", doc);
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
        final DocumentDTO parent = getDocument(folderId);

        LOGGER.debug("createDocumentFromSource(): parent document is {}", parent);

        if (parent == null || !isFolder(parent.getType())) {
            throw new CmisObjectNotFoundException("Parent is not a folder!");
        }
        DocumentDTO doc = null;
        try {
            // get source
            final DocumentDTO source = getDocument(sourceId);
            LOGGER.debug("createDocumentFromSource(): source document is {}", source);

            // check properties
//            final String typeId = getObjectTypeId(properties);
//
//            LOGGER.debug("createDocumentFromSource(): typeId is {}", typeId);
//            if (StringUtils.isBlank(typeId)) {
//                throw new CmisInvalidArgumentException("Type Id is not set!");
//            }
//            TypeDefinition type = getTypeDefinitionByTypeId(typeId);
//            checkCopyProperties(properties, BaseTypeId.CMIS_DOCUMENT.value(), type);
//
//            // check the name
//            String name = null;
//            if (properties != null && properties.getProperties() != null) {
//                name = FileBridgeUtils.getStringProperty(properties, PropertyIds.NAME);
//            }
//            if (name == null) {
//                name = source.getTitle();
//            }

            doc = crudService.add(new DocumentDTO(source.getTitle(), "document", context.getUsername()), context.getUsername());

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
        if (parent == null || !isFolder(parent.getType())) {
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
        LOGGER.debug("entering moveObject(context={}, objectId={}, targetFolderId={}, objectInfos = {})", context, objectId, targetFolderId, objectInfos);
        boolean userReadOnly = checkUser(context, true);

        if (objectId == null) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        // get the file and parent
        final DocumentDTO doc = getDocument(objectId.getValue());

        LOGGER.debug(" moveObject(): document for move {}", doc);
        if(doc == null || doc.getId() == null)
            throw new IllegalStateException(String.format("Document with id %s was not found", objectId));

        Long parentId = Long.parseLong(targetFolderId);
        doc.setParent(parentId.toString());
        crudService.setParent(doc);
        
        final DocumentDTO parent = getDocument(doc.getParent());//getFirstParent(doc.getId());

        LOGGER.debug(" moveObject(): parent document {}", parent);

        if (parent!=null){
            LOGGER.debug("moveObject(): removing exiting link with headId {} and tailId {}", parent.getId(), doc.getId());
            LinkDTO deletedLink = crudService.deleteLink(parent.getId(), doc.getId());
            LOGGER.debug("moveObject(): existing link {} has been deleted", deletedLink);
        }
        LinkDTO link = crudService.addLink(Long.parseLong(targetFolderId), doc.getId());

//        localDocumentDtoCache.put(objectId.getValue(), doc);
        LOGGER.debug("leaving moveObject(): new link {} has been created for object {}", link, doc);

        return compileObjectData(context, doc, parent, null, false, false, userReadOnly, objectInfos);
    }

    /**
     * CMIS setContentStream, deleteContentStream, and appendContentStream.
     */
    public void changeContentStream(CallContext context, Holder<String> objectId, Boolean overwriteFlag,
                                    ContentStream contentStream, boolean append) {
        LOGGER.debug("entering changeContentStream(context={}, objectId={}, overwriteFlag={}, contentStream = {}, append = {})", context, objectId, overwriteFlag, contentStream, append);
        checkUser(context, true);

        if (objectId == null) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        // get the file
        File file = getFile(objectId.getValue());
        LOGGER.debug("changeContentStream(): file {} ", file.getName() );
        if (!file.isFile()) {
            throw new CmisStreamNotSupportedException("Not a file!");
        }

        // check overwrite
        boolean owf = FileBridgeUtils.getBooleanParameter(overwriteFlag, true);
        LOGGER.debug("changeContentStream(): isOverwrite ? {} ", owf );
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

            DocumentDTO parent = getDocument(doc.getParent());//getFirstParent(doc.getId());

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
            LOGGER.trace("entering writeContent(doc={})", doc);
            LOGGER.trace("writeContent(): settingsNode {}, storageSettingsNode {}", settingsNode, storageActionsService != null ? storageActionsService.getClass() : null);
            final String filePath = storageActionsService.writeFile(storageManager.getRootName(settingsNode),  doc.getUuid(), org.apache.commons.io.IOUtils.toByteArray(stream));
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

                final DocumentDTO parent = getDocument(doc.getParent());//getFirstParent(doc.getId());

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
        final DocumentDTO parent = getParentDocument(doc.getParent());//getFirstParent(doc.getId());

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
        LOGGER.debug("entering updateProperties(context={}, objectId={}, properties={}, objectInfos ={})", context, objectId, properties, objectInfos);
        boolean userReadOnly = checkUser(context, true);

        // check object id
        if (objectId == null || objectId.getValue() == null) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }
        if(properties == null)
            throw new CmisInvalidArgumentException("Properties is null");
        // get the file or folder
        final DocumentDTO doc = getDocument(objectId.getValue());

        LOGGER.debug("updateProperties(): document for update: {}", doc);

        // check the properties
        String typeId = (isFolder(doc.getType()) ? BaseTypeId.CMIS_FOLDER.value() : BaseTypeId.CMIS_DOCUMENT.value());

        LOGGER.debug("updateProperties(): typeId is : {}", typeId);

        if (StringUtils.isBlank(typeId)) {
            throw new CmisInvalidArgumentException("Type Id is not set!");
        }
        TypeDefinition type = getTypeDefinitionByTypeId(typeId);

        LOGGER.debug("updateProperties(): type definition is : {}", type);
        checkUpdateProperties(properties, typeId, type);

        // get and check the new name
        final String newName = FileBridgeUtils.getStringProperty(properties, PropertyIds.NAME);
        LOGGER.debug("updateProperties(): new name : {}", newName);

        final String description = FileBridgeUtils.getStringProperty(properties, PropertyIds.DESCRIPTION);
        LOGGER.debug("updateProperties(): new description : {}", description);
        boolean isRename = (newName != null) && (!newName.equals(doc.getTitle()));
        boolean isUpdateDescription = description != null && !description.equals(doc.getDescription());

        boolean isUpdate = false;
        if (isRename) {
            doc.setTitle(newName);
            isUpdate = true;
        }
        if(isUpdateDescription){
            doc.setDescription(description);
            isUpdate = true;
        }
        if (isUpdate)
            crudService.update(doc ,context.getUsername());

        final DocumentDTO parent = getDocument(doc.getParent());
        LOGGER.debug("updateProperties(): parent document {}", parent);

//        localDocumentDtoCache.put(objectId.getValue(), doc);
        LOGGER.debug("leaving updateProperties(context={}, objectId={}, properties={}, objectInfos ={})", context, objectId, properties, objectInfos);
        return compileObjectData(context, doc, parent, null, false, false, userReadOnly, objectInfos);
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
        final DocumentDTO doc = getDocument(objectId);

        // set defaults if values not set
        boolean iaa = FileBridgeUtils.getBooleanParameter(includeAllowableActions, false);
        boolean iacl = FileBridgeUtils.getBooleanParameter(includeAcl, false);

        // split filter
        Set<String> filterCollection = FileBridgeUtils.splitFilter(filter);

        final DocumentDTO parent = getDocument(doc.getParent());
        LOGGER.debug("getObject(): parent document {}", parent);

        LOGGER.debug("leaving getObject(): found object {}", doc);
        // gather properties
        return compileObjectData(context, doc, parent, filterCollection, iaa, iacl, userReadOnly, objectInfos);
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

        byte[] contentByteArr = storageActionsService.readFile(doc.getFilePath());

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
        LOGGER.debug("entering getChildren(objectId={},  filter= {}, includeAllowableActions={}, includePathSegment={}, maxItems={}, skipCount={}, objectInfos={})",
                objectId, filter, includeAllowableActions, includePathSegment, maxItems, skipCount, objectInfos);
        boolean userReadOnly = checkUser(context, false);

        // split filter
        Set<String> filterCollection = FileBridgeUtils.splitFilter(filter);
        LOGGER.debug("getChildren(): filterCollection: {}", filterCollection);

        // set defaults if values not set
        boolean iaa = FileBridgeUtils.getBooleanParameter(includeAllowableActions, false);
        boolean ips = FileBridgeUtils.getBooleanParameter(includePathSegment, false);

        LOGGER.debug("getChildren(): Folder ID: {}", objectId);
        final Long parentId = Long.parseLong(objectId);
        List<DocumentDTO> docList = crudService.findAllByParent(parentId);

        LOGGER.debug("getChildren(): Found {} children.", docList != null ? docList.size() : null);
        ObjectInFolderListImpl result = new ObjectInFolderListImpl();
        if(docList == null){
            LOGGER.debug("getChildren(): document with parentid {} does not have children", parentId);
            return result;
        }

        // skip and max
        int skip = (skipCount == null ? 0 : skipCount.intValue());
        if (skip < 0) {
            skip = 0;
        }

        int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
        if (max < 0) {
            max = Integer.MAX_VALUE;
        }

        final DocumentDTO curdoc = getDocument(objectId);

        LOGGER.debug("getChildren(): current document {}.", curdoc);

        if (context.isObjectInfoRequired()) {
            final DocumentDTO parent = getDocument(curdoc.getParent());
            LOGGER.debug("getChildren(): parent document {}", parent);
            compileObjectData(context, curdoc, parent, null, false, false, userReadOnly, objectInfos);
        }

        // prepare result

        result.setObjects(new ArrayList<>());
        result.setHasMoreItems(false);
        int count = 0;

        LOGGER.debug("getChildren(): adding children to result...");
        for (DocumentDTO doc : docList){
            LOGGER.debug("getChildren(): child document {}", doc);
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

            final DocumentDTO parent = getParentDocument(doc.getParent());
            LOGGER.debug("getChildren(): parent document {}", parent);
            objectInFolder.setObject(compileObjectData(context, doc, parent, filterCollection, iaa, false, userReadOnly,
                    objectInfos));
            if (ips) {
                objectInFolder.setPathSegment(doc.getTitle());
            }

            result.getObjects().add(objectInFolder);
        }


        result.setNumItems(BigInteger.valueOf(count));
        LOGGER.debug("leaving getChildren(): result {}.", result);
        return result;
    }

    /**
     * CMIS getDescendants.
     */
    public List<ObjectInFolderContainer> getDescendants(CallContext context, String folderId, BigInteger depth,
                                                        String filter, Boolean includeAllowableActions, Boolean includePathSegment, ObjectInfoHandler objectInfos,
                                                        boolean foldersOnly) throws IOException {
        LOGGER.debug("entering getDescendants(folderId={}, depth={},  filter= {}, includeAllowableActions={}, includePathSegment={}, objectInfos={}, foldersOnly={})",
                folderId, depth, filter, includeAllowableActions, includePathSegment, objectInfos, foldersOnly);
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
        LOGGER.debug("getDescendants(): filterCollection: {}", filterCollection);
        // set defaults if values not set
        boolean iaa = FileBridgeUtils.getBooleanParameter(includeAllowableActions, false);
        boolean ips = FileBridgeUtils.getBooleanParameter(includePathSegment, false);

        // get the folder
        File folder = getFile(folderId);
        LOGGER.debug("getDescendants(): folder name: {}, isDirectory {}", folder.getName(), folder.isDirectory());
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


        LOGGER.debug("leaving getDescendants(): result {}.", result);
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
        LOGGER.debug("entering getObjectParents(objectId={},  filter= {}, includeAllowableActions={}, includeRelativePathSegment={}, objectInfos={})",
                objectId, filter, includeAllowableActions, includeRelativePathSegment, objectInfos);
        boolean userReadOnly = checkUser(context, false);

        // split filter
        Set<String> filterCollection = FileBridgeUtils.splitFilter(filter);
        LOGGER.debug("getObjectParents(): filterCollection: {}", filterCollection);

        // set defaults if values not set
        boolean iaa = FileBridgeUtils.getBooleanParameter(includeAllowableActions, false);
        boolean irps = FileBridgeUtils.getBooleanParameter(includeRelativePathSegment, false);

        // get the file or folder
        final DocumentDTO doc = getDocument(objectId);

        LOGGER.debug("getObjectParents():  document {}", doc);

        // don't climb above the root folder
        if (doc.getId()==0) {
            return Collections.emptyList();
        }

        final DocumentDTO parent = getDocument(doc.getParent());
        LOGGER.debug("getObjectParents(): parent document {}", parent);
        // set object info of the the object
        if (context.isObjectInfoRequired()) {

            compileObjectData(context, doc, parent, null, false, false, userReadOnly, objectInfos);
        }

        // get parent folder
        ObjectData object = compileObjectData(context, parent, null, filterCollection, iaa, false, userReadOnly, objectInfos);
        
        ObjectParentDataImpl result = new ObjectParentDataImpl();
        result.setObject(object);
        if (irps) {
            result.setRelativePathSegment(doc.getTitle());
        }
        LOGGER.debug("leaving getObjectParents(): result {}.", result);
        return Collections.<ObjectParentData> singletonList(result);
    }

    /**
     * CMIS getObjectByPath.
     */
    public ObjectData getObjectByPath(CallContext context, String folderPath, String filter,
                                      boolean includeAllowableActions, boolean includeACL, ObjectInfoHandler objectInfos) {
        LOGGER.debug("entering getObjectByPath(folderPath={},  filter= {}, includeAllowableActions={}, includeACL={}, objectInfos={})",
                folderPath, filter, includeAllowableActions, includeACL, objectInfos);
        boolean userReadOnly = checkUser(context, false);

        // check path
        if (folderPath == null || folderPath.length() == 0) {
            throw new CmisInvalidArgumentException("Invalid folder path!");
        }

        // split filter
        Set<String> filterCollection = FileBridgeUtils.splitFilter(filter);
        LOGGER.debug("getObjectByPath(): filterCollection: {}", filterCollection);



        Pageable pageable = new PageRequest(0, 1);
        List<DocumentDTO> dtos = crudService.findBySearchTerm(folderPath.substring(folderPath.lastIndexOf("/")+1, folderPath.length()), pageable);
        final DocumentDTO doc = dtos.iterator().next();
        LOGGER.debug("getObjectByPath():  document {}", doc);
        final DocumentDTO parent = getDocument(doc.getParent());
        LOGGER.debug("getObjectByPath(): parent document {}", parent);
        return compileObjectData(context, doc, parent, filterCollection, includeAllowableActions, includeACL, userReadOnly,
                objectInfos);
    }

    /**
     * CMIS query (simple IN_FOLDER queries only)
     */
    public ObjectList query(CallContext context, String statement, Boolean includeAllowableActions,
                            BigInteger maxItems, BigInteger skipCount, ObjectInfoHandler objectInfos) throws IOException {
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



    // --- helpers ---


    private DocumentDTO getDocument(final String objectId) {

        LOGGER.debug("entering getDocument(objectId ={})", objectId);


        if(StringUtils.isBlank(objectId))
            throw new IllegalArgumentException("Object id is null");

//        LOGGER.debug("try to find in local cache first");
//        DocumentDTO found = localDocumentDtoCache.get(objectId);

        DocumentDTO found = crudService.findById(Long.parseLong(objectId));


        if (found == null)
            throw new DocumentNotFoundException("Document was not found in database");

//        if(found == null || found.getId() == null) {
//            LOGGER.debug("document was not found in local cache, try to find it in database");
//            found = crudService.findById(Long.parseLong(objectId));
//
//            if (found == null)
//                throw new DocumentNotFoundException("Document was not found in database");
//            localDocumentDtoCache.put(objectId, found);
//        }
        LOGGER.debug("Found Document entry: {}", found);

        return found;
    }

//    private DocumentDTO getFirstParent(Long objectId) {
//        final List<DocumentDTO> docList = crudService.findParents(objectId);
//        return docList != null && docList.size() > 0 ? docList.get(0) : null;
//    }

    private DocumentDTO getParentDocument(final String objectId){
        LOGGER.debug("entering getParentDocument(objectId ={})", objectId);
        return objectId != null ? getDocument(objectId) : null;
    }


    /**
     * Checks if the user in the given context is valid for this repository and
     * if the user has the required permissions.
     */
    boolean checkUser(CallContext context, boolean writeRequired) {

        LOGGER.trace("entering checkUser(context={},writeRequired={})", context, writeRequired);
        if (context == null) {
            throw new CmisPermissionDeniedException("No user context!");
        }

        Boolean readOnly = readWriteUserMap.get(context.getUsername());

        if (readOnly == null) {
            throw new CmisPermissionDeniedException("Unknown user!");
        }

        if (readOnly && writeRequired) {
            throw new CmisPermissionDeniedException("No write permission!");
        }

        crudService.setUser(context.getUsername());


        LOGGER.trace("leaving checkUser(): is user {} readOnly? {}", context.getUsername(), readOnly);
        return readOnly;
    }


    @Override
    public String toString() {
        return "FileBridgeRepository{" +
                "repositoryId='" + repositoryId + '\'' +
                ", root=" + root +
                '}';
    }
}