package ru.doccloud.cmis.server.repository;

import org.apache.chemistry.opencmis.commons.BasicPermissions;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.*;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.doccloud.cmis.server.FileBridgeTypeManager;
import ru.doccloud.cmis.server.util.FileBridgeUtils;
import ru.doccloud.service.document.dto.DocumentDTO;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

import static ru.doccloud.cmis.server.util.FileBridgeUtils.*;

abstract class BridgeRepository {

    //    todo add loggind to this class
    private static final Logger LOGGER = LoggerFactory.getLogger(BridgeRepository.class);
    static final String ROOT_ID = "0";

    static final String USER_UNKNOWN = "<unknown>";

    private static final String ID_PREFIX = "0000000";

    static final int BUFFER_SIZE = 64 * 1024;

    static final Pattern IN_FOLDER_QUERY_PATTERN = Pattern
            .compile("(?i)select\\s+.+\\s+from\\s+(\\S*).*\\s+where\\s+in_folder\\('(.*)'\\)");

    /** Types. */
    FileBridgeTypeManager typeManager;
    /** Users. */
//    todo remove this map, take user from database once and store userinfo somewhere in session
//    Map<String, Boolean> readWriteUserMap;


    /** Root directory. */
    protected final File root;

    BridgeRepository(String rootPath,  FileBridgeTypeManager typeManager) {
        // check root folder
        if (StringUtils.isBlank(rootPath)) {
            throw new IllegalArgumentException("Invalid root folder!");
        }

        root = new File(rootPath);

        if (!root.isDirectory()) {
            throw new IllegalArgumentException("Root is not a directory!");
        }

        this.typeManager = typeManager;
    }

    boolean isFolder(String docType) {
        return "folder".equals(docType);
    }

    /**
     * Returns the id of a File object or throws an appropriate exception.
     */

    static String getId(Long docID) {
        return ID_PREFIX + docID;
    }

    String getId(final File file) {
        try {
            return fileToId(file);
        } catch (Exception e) {
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Converts an id to a File object. A simple and insecure implementation,
     * but good enough for now.
     */
    private File idToFile(final String id) throws IOException {
        if (StringUtils.isBlank(id)) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        if (id.equals(ROOT_ID)) {
            return root;
        }

        return new File(root, (new String(Base64.decode(id.getBytes("US-ASCII")), "UTF-8")).replace('/',
                File.separatorChar));
    }

    /**
     * Gathers all base properties of a file or folder.
     */
    private Properties compileProperties(CallContext context, File file, Set<String> orgfilter,
                                         ObjectInfoImpl objectInfo) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null!");
        }

        // we can't gather properties if the file or folder doesn't exist
        if (!file.exists()) {
            throw new CmisObjectNotFoundException("Object not found!");
        }

        boolean isDirectory = file.isDirectory();

        // id
        String id = fileToId(file);
        // name
        String name = file.getName();

        long createdTime = file.lastModified();
        long lastModifiedTime = file.lastModified();
        // change token - always null

        String rootId = isDirectory ? (!root.equals(file) ? (root.equals(file.getParentFile()) ? ROOT_ID : fileToId(file.getParentFile())): null) : null;
        String path = isDirectory ? getRepositoryPath(file):null;

        String title = isDirectory ? null : file.getName();

        boolean isFileNotEmpty = !isDirectory && file.length() != 0;

        String streamMimeType = isFileNotEmpty ? MimeTypes.getMIMEType(file) : null;

        String streamFileName = isFileNotEmpty ? file.getName() : null;

        BigInteger streamLenght = isFileNotEmpty ? BigInteger.valueOf(file.length()) : null;

        return compileProperties(context, rootId, orgfilter, objectInfo, isDirectory, id, name, null,
                USER_UNKNOWN, USER_UNKNOWN, createdTime, lastModifiedTime, path, title,
                isFileNotEmpty, streamMimeType, streamFileName, streamLenght);

    }



    /**
     * Gathers all base properties of a document.
     */
    private Properties compileProperties(CallContext context, DocumentDTO doc, DocumentDTO parentDoc, Set<String> orgfilter,
                                         ObjectInfoImpl objectInfo) {
        if (doc == null) {
            throw new IllegalArgumentException("File must not be null!");
        }

        boolean isDirectory = isFolder(doc.getType());
        String rootId = isDirectory ? (parentDoc != null ? getId(parentDoc.getId()) : null):null;
        String id = getId(doc.getId());
        String name = doc.getTitle();
        String createdBy = doc.getAuthor() != null ? doc.getAuthor() : USER_UNKNOWN;
        String modifiedBy = doc.getModifier() != null ? doc.getModifier() : USER_UNKNOWN;
        String path = isDirectory ? doc.getTitle() : null;

        String title = isDirectory ? null : doc.getTitle();

        boolean isDocHasContent = !isDirectory && !StringUtils.isBlank(doc.getFilePath());

        String streamMimeType = isDocHasContent ? doc.getFileMimeType() : null;

        String streamFileName = isDocHasContent ? doc.getTitle() : null;

        String description = doc.getDescription();

        BigInteger streamLenght = isDocHasContent ?
                (doc.getFileLength() != null ? BigInteger.valueOf( doc.getFileLength()) : null) : null;

        long createdTime = doc.getCreationTime().toDateTime().getMillis();
        long lastModifiedTime = doc.getModificationTime().toDateTime().getMillis();

        return compileProperties(context, rootId, orgfilter, objectInfo, isDirectory, id, name, description,
                createdBy, modifiedBy, createdTime, lastModifiedTime, path, title,
                isDocHasContent, streamMimeType, streamFileName, streamLenght);


    }

    private Properties compileProperties(CallContext context, String rootId, Set<String> orgfilter, ObjectInfoImpl objectInfo,
                                         boolean isDirectory, String id, String name, String description,
                                         String createdBy, String modifiedBy, Long createdTime, Long lastModifiedTime, String path,
                                         String title, boolean hasContent, String streamMimeType, String  streamFileName, BigInteger streamLenght){

        // copy filter
        Set<String> filter = (orgfilter == null ? null : new HashSet<>(orgfilter));
        // find base type
        String typeId = isDirectory ? BaseTypeId.CMIS_FOLDER.value() : BaseTypeId.CMIS_DOCUMENT.value();
        initObjectInfo(objectInfo, isDirectory, typeId);

        try {
            PropertiesImpl result = new PropertiesImpl();

            TypeDefinition type = getTypeDefinitionByTypeId(typeId);

            // id
            addPropertyId(result, typeId, filter, PropertyIds.OBJECT_ID, id, type);
            objectInfo.setId(id);

            // name
            addPropertyString(result, typeId, filter, PropertyIds.NAME, name, type);
            objectInfo.setName(name);

            // created and modified by
            addPropertyString(result, typeId, filter, PropertyIds.CREATED_BY, createdBy, type);
            addPropertyString(result, typeId, filter, PropertyIds.LAST_MODIFIED_BY, modifiedBy, type);
            objectInfo.setCreatedBy(USER_UNKNOWN);

            // creation and modification date
            GregorianCalendar created = FileBridgeUtils.millisToCalendar(createdTime);
            GregorianCalendar lastModified = FileBridgeUtils.millisToCalendar(lastModifiedTime);
            addPropertyDateTime(result, typeId, filter, PropertyIds.CREATION_DATE, created, type);
            addPropertyDateTime(result, typeId, filter, PropertyIds.LAST_MODIFICATION_DATE, lastModified, type);
            objectInfo.setCreationDate(lastModified);
            objectInfo.setLastModificationDate(lastModified);

            // change token - always null
            addPropertyString(result, typeId, filter, PropertyIds.CHANGE_TOKEN, null, type);

            // CMIS 1.1 properties
            if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
                addPropertyString(result, typeId, filter, PropertyIds.DESCRIPTION, description, type);
                addPropertyIdList(result, typeId, filter, PropertyIds.SECONDARY_OBJECT_TYPE_IDS, null, type);
            }
            addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID, typeId, type);
            addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID, typeId, type);

            // directory or file
            if (isDirectory) {
                addPropertyString(result, typeId, filter, PropertyIds.PATH, path, type);

                addPropertyId(result, typeId, filter, PropertyIds.PARENT_ID,
                        rootId, type);

                objectInfo.setHasParent(rootId !=null);

                addPropertyIdList(result, typeId, filter, PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, null, type);
            } else {
                // file properties
                compileConstantProperties(result, typeId, filter, type, context);

                addPropertyString(result, typeId, filter, PropertyIds.VERSION_LABEL, title, type);

                addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_MIME_TYPE, streamMimeType, type);
                addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, streamFileName, type);
                addPropertyBigInteger(result, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, streamLenght, type);

                objectInfo.setHasContent(hasContent);
                objectInfo.setContentType(streamMimeType);
                objectInfo.setFileName(streamFileName);
            }
            return result;
        } catch (CmisBaseException cbe) {
            throw cbe;
        } catch (Exception e) {
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Gather the children of a folder.
     */
    void gatherDescendants(CallContext context, File folder, List<ObjectInFolderContainer> list,
                           boolean foldersOnly, int depth, Set<String> filter, boolean includeAllowableActions,
                           boolean includePathSegments, boolean userReadOnly, ObjectInfoHandler objectInfos) throws IOException {
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


    TypeDefinition getTypeDefinitionByTypeId(String typeId){
        TypeDefinition type = typeManager.getInternalTypeDefinition(typeId);

        if (type == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        return type;
    }

    /**
     * Compiles an object type object from a file or folder.
     */
    ObjectData compileObjectData(CallContext context, File file, Set<String> filter,
                                 boolean includeAllowableActions, boolean includeAcl, boolean userReadOnly, ObjectInfoHandler objectInfos) throws IOException {
        ObjectDataImpl result = new ObjectDataImpl();
        ObjectInfoImpl objectInfo = new ObjectInfoImpl();

        result.setProperties(compileProperties(context, file, filter, objectInfo));

        if (includeAllowableActions) {
            result.setAllowableActions(compileAllowableActions(file, userReadOnly));
        }

        if (includeAcl) {
            result.setAcl(compileAcl(file, context.getUsername(), userReadOnly));
            result.setIsExactAcl(true);
        }

        if (context.isObjectInfoRequired()) {
            objectInfo.setObject(result);
            objectInfos.addObjectInfo(objectInfo);
        }

        return result;
    }

    /**
     * Compiles an object type object from a document.
     */
    ObjectData compileObjectData(CallContext context, DocumentDTO doc, DocumentDTO parentDoc, Set<String> filter,
                                 boolean includeAllowableActions, boolean includeAcl, boolean userReadOnly, ObjectInfoHandler objectInfos) {
        ObjectDataImpl result = new ObjectDataImpl();
        ObjectInfoImpl objectInfo = new ObjectInfoImpl();

        result.setProperties(compileProperties(context, doc, parentDoc, filter, objectInfo));

//        if (includeAllowableActions) {
//            //result.setAllowableActions(compileAllowableActions(file, userReadOnly));
//        }
//
//        if (includeAcl) {
//           // result.setAcl(compileAcl(file));
//           // result.setIsExactAcl(true);
//        }

        if (context.isObjectInfoRequired()) {
            objectInfo.setObject(result);
            objectInfos.addObjectInfo(objectInfo);
        }

        return result;
    }


    /**
     * Compiles the allowable actions for a file or folder.
     */
    AllowableActions compileAllowableActions(File file, boolean userReadOnly) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null!");
        }

        // we can't gather allowable actions if the file or folder doesn't exist
        if (!file.exists()) {
            throw new CmisObjectNotFoundException("Object not found!");
        }

        boolean isReadOnly = !file.canWrite();
        boolean isFolder = file.isDirectory();
        boolean isRoot = root.equals(file);

        Set<Action> aas = EnumSet.noneOf(Action.class);

        addAction(aas, Action.CAN_GET_OBJECT_PARENTS, !isRoot);
        addAction(aas, Action.CAN_GET_PROPERTIES, true);
        addAction(aas, Action.CAN_UPDATE_PROPERTIES, !userReadOnly && !isReadOnly);
        addAction(aas, Action.CAN_MOVE_OBJECT, !userReadOnly && !isRoot);
        addAction(aas, Action.CAN_DELETE_OBJECT, !userReadOnly && !isReadOnly && !isRoot);
        addAction(aas, Action.CAN_GET_ACL, true);

        if (isFolder) {
            addAction(aas, Action.CAN_GET_DESCENDANTS, true);
            addAction(aas, Action.CAN_GET_CHILDREN, true);
            addAction(aas, Action.CAN_GET_FOLDER_PARENT, !isRoot);
            addAction(aas, Action.CAN_GET_FOLDER_TREE, true);
            addAction(aas, Action.CAN_CREATE_DOCUMENT, !userReadOnly);
            addAction(aas, Action.CAN_CREATE_FOLDER, !userReadOnly);
            addAction(aas, Action.CAN_DELETE_TREE, !userReadOnly && !isReadOnly);
        } else {
            addAction(aas, Action.CAN_GET_CONTENT_STREAM, file.length() > 0);
            addAction(aas, Action.CAN_SET_CONTENT_STREAM, !userReadOnly && !isReadOnly);
            addAction(aas, Action.CAN_DELETE_CONTENT_STREAM, !userReadOnly && !isReadOnly);
            addAction(aas, Action.CAN_GET_ALL_VERSIONS, true);
        }

        AllowableActionsImpl result = new AllowableActionsImpl();
        result.setAllowableActions(aas);

        return result;
    }

    private void addAction(Set<Action> aas, Action action, boolean condition) {
        if (condition) {
            aas.add(action);
        }
    }

    /**
     * Compiles the ACL for a file or folder.
     */
    Acl compileAcl(File file, String userId, boolean userReadOnly) {
        AccessControlListImpl result = new AccessControlListImpl();
        result.setAces(new ArrayList<>());
        AccessControlPrincipalDataImpl principal = new AccessControlPrincipalDataImpl(userId);

        // create ACE
        AccessControlEntryImpl entry = new AccessControlEntryImpl();
        entry.setPrincipal(principal);
        entry.setPermissions(new ArrayList<String>());
        entry.getPermissions().add(BasicPermissions.READ);
        if (!userReadOnly && file.canWrite()) {
            entry.getPermissions().add(BasicPermissions.WRITE);
            entry.getPermissions().add(BasicPermissions.ALL);
        }

        entry.setDirect(true);

        // add ACE
        result.getAces().add(entry);

//        for (Map.Entry<String, Boolean> ue : readWriteUserMap.entrySet()) {
//            // create principal
//            AccessControlPrincipalDataImpl principal = new AccessControlPrincipalDataImpl(ue.getKey());
//
//            // create ACE
//            AccessControlEntryImpl entry = new AccessControlEntryImpl();
//            entry.setPrincipal(principal);
//            entry.setPermissions(new ArrayList<String>());
//            entry.getPermissions().add(BasicPermissions.READ);
//            if (!ue.getValue() && file.canWrite()) {
//                entry.getPermissions().add(BasicPermissions.WRITE);
//                entry.getPermissions().add(BasicPermissions.ALL);
//            }
//
//            entry.setDirect(true);
//
//            // add ACE
//            result.getAces().add(entry);
//        }

        return result;
    }


    private void compileConstantProperties(PropertiesImpl result, String typeId, Set<String> filter, TypeDefinition type, CallContext context){
        addPropertyBoolean(result, typeId, filter, PropertyIds.IS_IMMUTABLE, false, type);
        addPropertyBoolean(result, typeId, filter, PropertyIds.IS_LATEST_VERSION, true, type);
        addPropertyBoolean(result, typeId, filter, PropertyIds.IS_MAJOR_VERSION, true, type);
        addPropertyBoolean(result, typeId, filter, PropertyIds.IS_LATEST_MAJOR_VERSION, true, type);
        if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
            addPropertyBoolean(result, typeId, filter, PropertyIds.IS_PRIVATE_WORKING_COPY, false, type);
        }

        addPropertyBoolean(result, typeId, filter, PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false, type);

        addPropertyString(result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, null, type);
        addPropertyString(result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, null,type);
        addPropertyString(result, typeId, filter, PropertyIds.CHECKIN_COMMENT, "", type);

        addPropertyId(result, typeId, filter, PropertyIds.CONTENT_STREAM_ID, null, type);
    }


    private void initObjectInfo(ObjectInfoImpl objectInfo, boolean isDirectory, String typeId){
        objectInfo.setTypeId(typeId);
        objectInfo.setHasAcl(true);
        objectInfo.setVersionSeriesId(null);
        objectInfo.setIsCurrentVersion(true);
        objectInfo.setRelationshipSourceIds(null);
        objectInfo.setRelationshipTargetIds(null);
        objectInfo.setRenditionInfos(null);
        objectInfo.setSupportsPolicies(false);
        objectInfo.setSupportsRelationships(false);
        objectInfo.setWorkingCopyId(null);
        objectInfo.setWorkingCopyOriginalId(null);
        objectInfo.setContentType(null);
        objectInfo.setFileName(null);
        objectInfo.setBaseType(isDirectory ? BaseTypeId.CMIS_FOLDER : BaseTypeId.CMIS_DOCUMENT);
        objectInfo.setHasContent(!isDirectory);
        objectInfo.setSupportsDescendants(isDirectory);
        objectInfo.setSupportsFolderTree(isDirectory);
    }

    /**
     * Creates a File object from an id. A simple and insecure implementation,
     * but good enough for now.
     */
    private String fileToId(final File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File is not valid!");
        }

        if (root.equals(file)) {
            return ROOT_ID;
        }

        String path = getRepositoryPath(file);

        return Base64.encodeBytes(path.getBytes("UTF-8"));
    }

    private String getRepositoryPath(final File file) {
        String path = file.getAbsolutePath().substring(root.getAbsolutePath().length())
                .replace(File.separatorChar, '/');
        if (path.length() == 0) {
            path = "/";
        } else if (path.charAt(0) != '/') {
            path = "/" + path;
        }
        return path;
    }

    /**
     * Returns the File object by id or throws an appropriate exception.
     */
    File getFile(final String id) {
        try {
            return idToFile(id);
        } catch (Exception e) {
            throw new CmisObjectNotFoundException(e.getMessage(), e);
        }
    }

    /**
     * Checks if a folder is empty. A folder is considered as empty if no files
     * or only the shadow file reside in the folder.
     *
     * @param folder
     *            the folder
     *
     * @return <code>true</code> if the folder is empty.
     */
    private boolean isFolderEmpty(final File folder) {
        if (!folder.isDirectory()) {
            return true;
        }

        String[] fileNames = folder.list();

        return (fileNames == null) || (fileNames.length == 0);

    }

    abstract boolean checkUser(CallContext context, boolean writeRequired);
}