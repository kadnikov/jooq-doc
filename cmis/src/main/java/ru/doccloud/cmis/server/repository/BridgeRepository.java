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
import java.util.concurrent.ConcurrentHashMap;
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


//    final DocumentCrudService crudService;

    /** Types. */
    FileBridgeTypeManager typeManager;
    /** Users. */
    Map<String, Boolean> readWriteUserMap;


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

//        this.crudService = crudService;
        this.typeManager = typeManager;

        // set up read-write user map
        readWriteUserMap = new ConcurrentHashMap<>();
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
                                         ObjectInfoImpl objectInfo) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null!");
        }

        // we can't gather properties if the file or folder doesn't exist
        if (!file.exists()) {
            throw new CmisObjectNotFoundException("Object not found!");
        }

        // copy filter
        Set<String> filter = (orgfilter == null ? null : new HashSet<>(orgfilter));

        // find base type
        String typeId = file.isDirectory() ? BaseTypeId.CMIS_FOLDER.value() : BaseTypeId.CMIS_DOCUMENT.value();
        initObjectInfo(objectInfo, file.isDirectory(), typeId);


        // exercise 3.3
        try {
            PropertiesImpl result = new PropertiesImpl();

            TypeDefinition type = getTypeDefinitionByTypeId(typeId);

            // id
            String id = fileToId(file);
            addPropertyId(result, typeId, filter, PropertyIds.OBJECT_ID, id, type);
            objectInfo.setId(id);

            // name
            String name = file.getName();
            addPropertyString(result, typeId, filter, PropertyIds.NAME, name, type);
            objectInfo.setName(name);

            // created and modified by
            addPropertyString(result, typeId, filter, PropertyIds.CREATED_BY, USER_UNKNOWN, type);
            addPropertyString(result, typeId, filter, PropertyIds.LAST_MODIFIED_BY, USER_UNKNOWN, type);
            objectInfo.setCreatedBy(USER_UNKNOWN);

            // creation and modification date
            GregorianCalendar lastModified = FileBridgeUtils.millisToCalendar(file.lastModified());
            addPropertyDateTime(result, typeId, filter, PropertyIds.CREATION_DATE, lastModified, type);
            addPropertyDateTime(result, typeId, filter, PropertyIds.LAST_MODIFICATION_DATE, lastModified, type);
            objectInfo.setCreationDate(lastModified);
            objectInfo.setLastModificationDate(lastModified);

            // change token - always null
            addPropertyString(result, typeId, filter, PropertyIds.CHANGE_TOKEN, null, type);

            // CMIS 1.1 properties
            if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
                addPropertyString(result, typeId, filter, PropertyIds.DESCRIPTION, null, type);
                addPropertyIdList(result, typeId, filter, PropertyIds.SECONDARY_OBJECT_TYPE_IDS, null, type);
            }

            addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID, typeId, type);
            addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID, typeId, type);
            // directory or file
            if (file.isDirectory()) {
                String path = getRepositoryPath(file);
                addPropertyString(result, typeId, filter, PropertyIds.PATH, path, type);

                addPropertyId(result, typeId, filter, PropertyIds.PARENT_ID,
                        !root.equals(file) ? (root.equals(file.getParentFile()) ? ROOT_ID : fileToId(file.getParentFile())): null, type);

                objectInfo.setHasParent(!root.equals(file));

                addPropertyIdList(result, typeId, filter, PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, null, type);
            } else {
                // file properties
                compileConstantBooleanProperties(result, typeId, filter, type, context);

                compileConstantStringProperties(result, typeId, filter, type);

                addPropertyString(result, typeId, filter, PropertyIds.VERSION_LABEL, file.getName(), type);

                boolean isFileNotEmpty = file.length() != 0;

                addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_MIME_TYPE, isFileNotEmpty ? MimeTypes.getMIMEType(file) : null, type);
                addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, isFileNotEmpty ? file.getName() : null, type);
                addPropertyBigInteger(result, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, isFileNotEmpty ? BigInteger.valueOf(file.length()) : null, type);

                objectInfo.setHasContent(isFileNotEmpty);
                objectInfo.setContentType(isFileNotEmpty ? MimeTypes.getMIMEType(file) : null);
                objectInfo.setFileName(isFileNotEmpty ? file.getName(): null);

                addPropertyId(result, typeId, filter, PropertyIds.VERSION_SERIES_ID, fileToId(file), type);
                addPropertyId(result, typeId, filter, PropertyIds.CONTENT_STREAM_ID, null, type);
            }

            return result;
        } catch (CmisBaseException cbe) {
            throw cbe;
        } catch (Exception e) {
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Gathers all base properties of a document.
     */
    private Properties compileProperties(CallContext context, DocumentDTO doc, DocumentDTO parentDoc, Set<String> orgfilter,
                                         ObjectInfoImpl objectInfo) {
        if (doc == null) {
            throw new IllegalArgumentException("File must not be null!");
        }

        // copy filter
        Set<String> filter = (orgfilter == null ? null : new HashSet<>(orgfilter));

        boolean isDirectory = isFolder(doc.getType());
        // find base type
        String typeId = isDirectory ? BaseTypeId.CMIS_FOLDER.value() : BaseTypeId.CMIS_DOCUMENT.value();
        initObjectInfo(objectInfo, isDirectory, typeId);
        // identify if the file is a doc or a folder/directory


        // exercise 3.3
        try {
            PropertiesImpl result = new PropertiesImpl();

            TypeDefinition type = getTypeDefinitionByTypeId(typeId);

            // id
            String id = getId(doc.getId());

            addPropertyId(result, typeId, filter, PropertyIds.OBJECT_ID, id, type);
            objectInfo.setId(id);

            // name
            String name = doc.getTitle();
            addPropertyString(result, typeId, filter, PropertyIds.NAME, name, type);
            objectInfo.setName(name);

            // created and modified by
            String createdBy = doc.getAuthor() != null ? doc.getAuthor() : USER_UNKNOWN;
            addPropertyString(result, typeId, filter, PropertyIds.CREATED_BY, createdBy, type);

            String modifiedBy = doc.getModifier() != null ? doc.getModifier() : USER_UNKNOWN;
            addPropertyString(result, typeId, filter, PropertyIds.LAST_MODIFIED_BY, modifiedBy, type);
            objectInfo.setCreatedBy(createdBy);

            // creation and modification date
            GregorianCalendar created = FileBridgeUtils.millisToCalendar(doc.getCreationTime().toDateTime().getMillis());
            addPropertyDateTime(result, typeId, filter, PropertyIds.CREATION_DATE, created, type);
            GregorianCalendar lastModified = FileBridgeUtils.millisToCalendar(doc.getModificationTime().toDateTime().getMillis());
            addPropertyDateTime(result, typeId, filter, PropertyIds.LAST_MODIFICATION_DATE, lastModified, type);
            objectInfo.setCreationDate(created);
            objectInfo.setLastModificationDate(lastModified);

            // change token - always null
            addPropertyString(result, typeId, filter, PropertyIds.CHANGE_TOKEN, null, type);

            // CMIS 1.1 properties
            if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
                addPropertyString(result, typeId, filter, PropertyIds.DESCRIPTION, doc.getDescription(), type);
                addPropertyIdList(result, typeId, filter, PropertyIds.SECONDARY_OBJECT_TYPE_IDS, null, type);
            }

            addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID, typeId, type);
            addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID, typeId, type);


            // directory or file
            if (isDirectory) {
                String path = doc.getTitle();
                addPropertyString(result, typeId, filter, PropertyIds.PATH, "/"+path, type);

                addPropertyId(result, typeId, filter, PropertyIds.PARENT_ID, parentDoc != null ? getId(parentDoc.getId()) : null, type);
                objectInfo.setHasParent(parentDoc != null);

                addPropertyIdList(result, typeId, filter, PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, null, type);
            } else {
                // file properties
                compileConstantBooleanProperties(result, typeId, filter, type, context);

                compileConstantStringProperties(result, typeId, filter, type);

                addPropertyString(result, typeId, filter, PropertyIds.VERSION_LABEL, doc.getTitle(), type);


                addPropertyId(result, typeId, filter, PropertyIds.VERSION_SERIES_ID, getId(doc.getId()), type);


                boolean isDocHasContent = !StringUtils.isBlank(doc.getFilePath());

                addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_MIME_TYPE, isDocHasContent ? doc.getFileMimeType() : null, type);
                addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, isDocHasContent ? doc.getTitle() : null, type);

                addPropertyBigInteger(result, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, isDocHasContent ?
                        (doc.getFileLength() != null ? BigInteger.valueOf( doc.getFileLength()) : null) : null, type);


                objectInfo.setHasContent(isDocHasContent);
                objectInfo.setContentType(isDocHasContent ? doc.getFileMimeType(): null);
                objectInfo.setFileName(isDocHasContent ?doc.getTitle() : null);

                addPropertyId(result, typeId, filter, PropertyIds.CONTENT_STREAM_ID, null, type);
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
                                 boolean includeAllowableActions, boolean includeAcl, boolean userReadOnly, ObjectInfoHandler objectInfos) {
        ObjectDataImpl result = new ObjectDataImpl();
        ObjectInfoImpl objectInfo = new ObjectInfoImpl();

        result.setProperties(compileProperties(context, file, filter, objectInfo));

        if (includeAllowableActions) {
            result.setAllowableActions(compileAllowableActions(file, userReadOnly));
        }

        if (includeAcl) {
            result.setAcl(compileAcl(file));
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
    Acl compileAcl(File file) {
        AccessControlListImpl result = new AccessControlListImpl();
        result.setAces(new ArrayList<Ace>());

        for (Map.Entry<String, Boolean> ue : readWriteUserMap.entrySet()) {
            // create principal
            AccessControlPrincipalDataImpl principal = new AccessControlPrincipalDataImpl(ue.getKey());

            // create ACE
            AccessControlEntryImpl entry = new AccessControlEntryImpl();
            entry.setPrincipal(principal);
            entry.setPermissions(new ArrayList<String>());
            entry.getPermissions().add(BasicPermissions.READ);
            if (!ue.getValue() && file.canWrite()) {
                entry.getPermissions().add(BasicPermissions.WRITE);
                entry.getPermissions().add(BasicPermissions.ALL);
            }

            entry.setDirect(true);

            // add ACE
            result.getAces().add(entry);
        }

        return result;
    }


    private void compileConstantBooleanProperties(PropertiesImpl result, String typeId, Set<String> filter, TypeDefinition type, CallContext context){
        addPropertyBoolean(result, typeId, filter, PropertyIds.IS_IMMUTABLE, false, type);
        addPropertyBoolean(result, typeId, filter, PropertyIds.IS_LATEST_VERSION, true, type);
        addPropertyBoolean(result, typeId, filter, PropertyIds.IS_MAJOR_VERSION, true, type);
        addPropertyBoolean(result, typeId, filter, PropertyIds.IS_LATEST_MAJOR_VERSION, true, type);
        if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
            addPropertyBoolean(result, typeId, filter, PropertyIds.IS_PRIVATE_WORKING_COPY, false, type);
        }

        addPropertyBoolean(result, typeId, filter, PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false, type);
    }

    private void compileConstantStringProperties(PropertiesImpl result, String typeId, Set<String> filter, TypeDefinition type){
        addPropertyString(result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, null, type);
        addPropertyString(result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, null,type);
        addPropertyString(result, typeId, filter, PropertyIds.CHECKIN_COMMENT, "", type);
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
