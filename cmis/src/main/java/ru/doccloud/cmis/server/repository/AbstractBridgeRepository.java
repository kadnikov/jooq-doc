package ru.doccloud.cmis.server.repository;


import org.apache.chemistry.opencmis.commons.BasicPermissions;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.*;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.*;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
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
import ru.doccloud.document.dto.DocumentDTO;
import ru.doccloud.document.service.DocumentCrudService;

import java.io.File;
import java.util.*;

import static ru.doccloud.cmis.server.util.FileBridgeUtils.*;

public abstract class AbstractBridgeRepository extends BridgeRepository{

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBridgeRepository.class);
    /** Repository id. */
    protected final String repositoryId;

    final DocumentCrudService crudService;

    /** Types. */
    FileBridgeTypeManager typeManager;
    /** Users. */
    Map<String, Boolean> readWriteUserMap;


    /** CMIS 1.0 repository info. */
    private final RepositoryInfo repositoryInfo10;
    /** CMIS 1.1 repository info. */
    private final RepositoryInfo repositoryInfo11;

    AbstractBridgeRepository(String repositoryId, String rootPath, DocumentCrudService crudService) {
        super(rootPath);

        // check repository id
        if (StringUtils.isBlank(repositoryId)) {
            throw new IllegalArgumentException("Invalid repository id!");
        }

        this.repositoryId = repositoryId;
        // set up repository infos
        repositoryInfo10 = createRepositoryInfo(CmisVersion.CMIS_1_0);
        repositoryInfo11 = createRepositoryInfo(CmisVersion.CMIS_1_1);
        this.crudService = crudService;
    }


    /**
     * Returns the id of this repository.
     */
    public String getRepositoryId() {
        return repositoryId;
    }

    /**
     * Returns the root directory of this repository
     */
    File getRootDirectory() {
        return root;
    }



    /**
     * Gathers all base properties of a document.
     */
    private Properties compileProperties(CallContext context, DocumentDTO doc, Set<String> orgfilter,
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
            String createdBy = doc.getAuthor();
            if (createdBy==null){
                createdBy = USER_UNKNOWN;
            }
            addPropertyString(result, typeId, filter, PropertyIds.CREATED_BY, createdBy, type);

            String modifiedBy = doc.getModifier();
            if (modifiedBy==null){
                modifiedBy = USER_UNKNOWN;
            }
            addPropertyString(result, typeId, filter, PropertyIds.LAST_MODIFIED_BY, modifiedBy, type);
            objectInfo.setCreatedBy(createdBy);

            // creation and modification date
            LOGGER.debug("doc.getCreationTime() - {}", doc.getCreationTime());
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

            // directory or file
            if (isFolder(doc.getType())) {
                // base type and type name
                addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_FOLDER.value(), type);
                addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_FOLDER.value(), type);
                String path = doc.getTitle();
                addPropertyString(result, typeId, filter, PropertyIds.PATH, "/"+path, type);

                // folder properties
                if (doc.getId()!=0) {
                    DocumentDTO firstParentDoc = getFirstParent(doc.getId());
                    addPropertyId(result, typeId, filter, PropertyIds.PARENT_ID, getId(firstParentDoc.getId()), type);
                    objectInfo.setHasParent(true);
                } else {
                    addPropertyId(result, typeId, filter, PropertyIds.PARENT_ID, null, type);
                    objectInfo.setHasParent(false);
                }

                addPropertyIdList(result, typeId, filter, PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, null, type);
            } else {
                // base type and type name
                addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value(), type);
                addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value(), type);

                // file properties
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_IMMUTABLE, false, type);
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_LATEST_VERSION, true, type);
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_MAJOR_VERSION, true, type);
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_LATEST_MAJOR_VERSION, true, type);
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_LABEL, doc.getTitle(), type);
                addPropertyId(result, typeId, filter, PropertyIds.VERSION_SERIES_ID, getId(doc.getId()), type);
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false, type);
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, null, type);
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, null,type);
                addPropertyString(result, typeId, filter, PropertyIds.CHECKIN_COMMENT, "", type);
                if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
                    addPropertyBoolean(result, typeId, filter, PropertyIds.IS_PRIVATE_WORKING_COPY, false, type);
                }

                if (doc.getFilePath()==null) {
                    addPropertyBigInteger(result, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, null, type);
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_MIME_TYPE, null, type);
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, null, type);

                    objectInfo.setHasContent(false);
                    objectInfo.setContentType(null);
                    objectInfo.setFileName(null);
                } else {
                    if (doc.getFileLength()==null){
                        addPropertyBigInteger(result, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, null, type);
                    }else{
                        addPropertyInteger(result, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, doc.getFileLength(), type);
                    }
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_MIME_TYPE, doc.getFileMimeType(), type);
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, doc.getTitle(), type);

                    objectInfo.setHasContent(true);
                    objectInfo.setContentType(doc.getFileMimeType());
                    objectInfo.setFileName(doc.getTitle());
                }

                addPropertyId(result, typeId, filter, PropertyIds.CONTENT_STREAM_ID, null, type);
            }

            return result;
        } catch (CmisBaseException cbe) {
            throw cbe;
        } catch (Exception e) {
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }


    private void initObjectInfo(ObjectInfoImpl objectInfo, boolean isDirectory, String typeId){
        if (isDirectory) {
            objectInfo.setBaseType(BaseTypeId.CMIS_FOLDER);
            objectInfo.setTypeId(typeId);
            objectInfo.setContentType(null);
            objectInfo.setFileName(null);
            objectInfo.setHasAcl(true);
            objectInfo.setHasContent(false);
            objectInfo.setVersionSeriesId(null);
            objectInfo.setIsCurrentVersion(true);
            objectInfo.setRelationshipSourceIds(null);
            objectInfo.setRelationshipTargetIds(null);
            objectInfo.setRenditionInfos(null);
            objectInfo.setSupportsDescendants(true);
            objectInfo.setSupportsFolderTree(true);
            objectInfo.setSupportsPolicies(false);
            objectInfo.setSupportsRelationships(false);
            objectInfo.setWorkingCopyId(null);
            objectInfo.setWorkingCopyOriginalId(null);
        } else {
            objectInfo.setBaseType(BaseTypeId.CMIS_DOCUMENT);
            objectInfo.setTypeId(typeId);
            objectInfo.setHasAcl(true);
            objectInfo.setHasContent(true);
            objectInfo.setHasParent(true);
            objectInfo.setVersionSeriesId(null);
            objectInfo.setIsCurrentVersion(true);
            objectInfo.setRelationshipSourceIds(null);
            objectInfo.setRelationshipTargetIds(null);
            objectInfo.setRenditionInfos(null);
            objectInfo.setSupportsDescendants(false);
            objectInfo.setSupportsFolderTree(false);
            objectInfo.setSupportsPolicies(false);
            objectInfo.setSupportsRelationships(false);
            objectInfo.setWorkingCopyId(null);
            objectInfo.setWorkingCopyOriginalId(null);
        }
    }

    private RepositoryInfo createRepositoryInfo(CmisVersion cmisVersion) {
        assert cmisVersion != null;

        RepositoryInfoImpl repositoryInfo = new RepositoryInfoImpl();

        repositoryInfo.setId(repositoryId);
        repositoryInfo.setName(repositoryId);
        repositoryInfo.setDescription(repositoryId);

        // exercise 1.1
        repositoryInfo.setCmisVersionSupported(cmisVersion.value());

        // exercise 1.2
        repositoryInfo.setProductName("DocCloud Server");
        repositoryInfo.setProductVersion("1.0");
        repositoryInfo.setVendorName("DocCloud");

        // exercise 1.3
        repositoryInfo.setRootFolder(ROOT_ID);

        repositoryInfo.setThinClientUri("");
        repositoryInfo.setChangesIncomplete(true);

        RepositoryCapabilitiesImpl capabilities = new RepositoryCapabilitiesImpl();
        capabilities.setCapabilityAcl(CapabilityAcl.DISCOVER);
        capabilities.setAllVersionsSearchable(false);
        capabilities.setCapabilityJoin(CapabilityJoin.NONE);
        capabilities.setSupportsMultifiling(false);
        capabilities.setSupportsUnfiling(false);
        capabilities.setSupportsVersionSpecificFiling(false);
        capabilities.setIsPwcSearchable(false);
        capabilities.setIsPwcUpdatable(false);
        capabilities.setCapabilityQuery(CapabilityQuery.METADATAONLY);
        capabilities.setCapabilityChanges(CapabilityChanges.NONE);
        capabilities.setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates.ANYTIME);
        capabilities.setSupportsGetDescendants(true);
        capabilities.setSupportsGetFolderTree(true);
        capabilities.setCapabilityRendition(CapabilityRenditions.NONE);

        if (cmisVersion != CmisVersion.CMIS_1_0) {
            capabilities.setCapabilityOrderBy(CapabilityOrderBy.NONE);

            NewTypeSettableAttributesImpl typeSetAttributes = new NewTypeSettableAttributesImpl();
            typeSetAttributes.setCanSetControllableAcl(false);
            typeSetAttributes.setCanSetControllablePolicy(false);
            typeSetAttributes.setCanSetCreatable(false);
            typeSetAttributes.setCanSetDescription(false);
            typeSetAttributes.setCanSetDisplayName(false);
            typeSetAttributes.setCanSetFileable(false);
            typeSetAttributes.setCanSetFulltextIndexed(false);
            typeSetAttributes.setCanSetId(false);
            typeSetAttributes.setCanSetIncludedInSupertypeQuery(false);
            typeSetAttributes.setCanSetLocalName(false);
            typeSetAttributes.setCanSetLocalNamespace(false);
            typeSetAttributes.setCanSetQueryable(false);
            typeSetAttributes.setCanSetQueryName(false);

            capabilities.setNewTypeSettableAttributes(typeSetAttributes);

            CreatablePropertyTypesImpl creatablePropertyTypes = new CreatablePropertyTypesImpl();
            capabilities.setCreatablePropertyTypes(creatablePropertyTypes);
        }

        repositoryInfo.setCapabilities(capabilities);

        AclCapabilitiesDataImpl aclCapability = new AclCapabilitiesDataImpl();
        aclCapability.setSupportedPermissions(SupportedPermissions.BASIC);
        aclCapability.setAclPropagation(AclPropagation.OBJECTONLY);

        // permissions
        List<PermissionDefinition> permissions = new ArrayList<PermissionDefinition>();
        permissions.add(createPermission(BasicPermissions.READ, "Read"));
        permissions.add(createPermission(BasicPermissions.WRITE, "Write"));
        permissions.add(createPermission(BasicPermissions.ALL, "All"));
        aclCapability.setPermissionDefinitionData(permissions);

        // mapping
        List<PermissionMapping> list = new ArrayList<>();
        list.add(createMapping(PermissionMapping.CAN_CREATE_DOCUMENT_FOLDER, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_CREATE_FOLDER_FOLDER, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_DELETE_CONTENT_DOCUMENT, BasicPermissions.WRITE));
        list.add(createMapping(PermissionMapping.CAN_DELETE_OBJECT, BasicPermissions.ALL));
        list.add(createMapping(PermissionMapping.CAN_DELETE_TREE_FOLDER, BasicPermissions.ALL));
        list.add(createMapping(PermissionMapping.CAN_GET_ACL_OBJECT, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_GET_ALL_VERSIONS_VERSION_SERIES, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_GET_CHILDREN_FOLDER, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_GET_DESCENDENTS_FOLDER, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_GET_FOLDER_PARENT_OBJECT, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_GET_PARENTS_FOLDER, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_GET_PROPERTIES_OBJECT, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_MOVE_OBJECT, BasicPermissions.WRITE));
        list.add(createMapping(PermissionMapping.CAN_MOVE_SOURCE, BasicPermissions.READ));
        list.add(createMapping(PermissionMapping.CAN_MOVE_TARGET, BasicPermissions.WRITE));
        list.add(createMapping(PermissionMapping.CAN_SET_CONTENT_DOCUMENT, BasicPermissions.WRITE));
        list.add(createMapping(PermissionMapping.CAN_UPDATE_PROPERTIES_OBJECT, BasicPermissions.WRITE));
        list.add(createMapping(PermissionMapping.CAN_VIEW_CONTENT_OBJECT, BasicPermissions.READ));
        Map<String, PermissionMapping> map = new LinkedHashMap<String, PermissionMapping>();
        for (PermissionMapping pm : list) {
            map.put(pm.getKey(), pm);
        }
        aclCapability.setPermissionMappingData(map);

        repositoryInfo.setAclCapabilities(aclCapability);

        return repositoryInfo;
    }


    DocumentDTO getFirstParent(Long objectId) {
        final List<DocumentDTO> docList = crudService.findParents(objectId);
        return docList != null && docList.size() > 0 ? docList.get(0) : null;
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

            // directory or file
            if (file.isDirectory()) {
                // base type and type name
                addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_FOLDER.value(), type);
                addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_FOLDER.value(), type);
                String path = getRepositoryPath(file);
                addPropertyString(result, typeId, filter, PropertyIds.PATH, path, type);

                // folder properties
                if (!root.equals(file)) {
                    addPropertyId(result, typeId, filter, PropertyIds.PARENT_ID,
                            (root.equals(file.getParentFile()) ? ROOT_ID : fileToId(file.getParentFile())), type);
                    objectInfo.setHasParent(true);
                } else {
                    addPropertyId(result, typeId, filter, PropertyIds.PARENT_ID, null, type);
                    objectInfo.setHasParent(false);
                }

                addPropertyIdList(result, typeId, filter, PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, null, type);
            } else {
                // base type and type name
                addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value(), type);
                addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value(), type);

                // file properties
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_IMMUTABLE, false, type);
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_LATEST_VERSION, true, type);
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_MAJOR_VERSION, true, type);
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_LATEST_MAJOR_VERSION, true, type);
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_LABEL, file.getName(), type);
                addPropertyId(result, typeId, filter, PropertyIds.VERSION_SERIES_ID, fileToId(file), type);
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false, type);
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, null, type);
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, null, type);
                addPropertyString(result, typeId, filter, PropertyIds.CHECKIN_COMMENT, "", type);
                if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
                    addPropertyBoolean(result, typeId, filter, PropertyIds.IS_PRIVATE_WORKING_COPY, false, type);
                }

                if (file.length() == 0) {
                    addPropertyBigInteger(result, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, null, type);
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_MIME_TYPE, null, type);
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, null, type);

                    objectInfo.setHasContent(false);
                    objectInfo.setContentType(null);
                    objectInfo.setFileName(null);
                } else {
                    addPropertyInteger(result, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, file.length(), type);
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_MIME_TYPE,
                            MimeTypes.getMIMEType(file), type);
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, file.getName(), type);

                    objectInfo.setHasContent(true);
                    objectInfo.setContentType(MimeTypes.getMIMEType(file));
                    objectInfo.setFileName(file.getName());
                }

                addPropertyId(result, typeId, filter, PropertyIds.CONTENT_STREAM_ID, null, type);
            }

            return result;
        } catch (CmisBaseException cbe) {
            throw cbe;
        } catch (Exception e) {
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }


    private PermissionDefinition createPermission(String permission, String description) {
        PermissionDefinitionDataImpl pd = new PermissionDefinitionDataImpl();
        pd.setId(permission);
        pd.setDescription(description);

        return pd;
    }

    private PermissionMapping createMapping(String key, String permission) {
        PermissionMappingDataImpl pm = new PermissionMappingDataImpl();
        pm.setKey(key);
        pm.setPermissions(Collections.singletonList(permission));

        return pm;
    }

    /**
     * CMIS getRepositoryInfo.
     */
    public RepositoryInfo getRepositoryInfo(CallContext context) {
        checkUser(context, false);

        LOGGER.trace("getRepositoryInfo(): cmisVersion {})", context.getCmisVersion());
        if (context.getCmisVersion() == CmisVersion.CMIS_1_0) {
            return repositoryInfo10;
        } else {
            return repositoryInfo11;
        }
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
    ObjectData compileObjectData(CallContext context, DocumentDTO doc, Set<String> filter,
                                 boolean includeAllowableActions, boolean includeAcl, boolean userReadOnly, ObjectInfoHandler objectInfos) {
        ObjectDataImpl result = new ObjectDataImpl();
        ObjectInfoImpl objectInfo = new ObjectInfoImpl();

        result.setProperties(compileProperties(context, doc, filter, objectInfo));

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
}



