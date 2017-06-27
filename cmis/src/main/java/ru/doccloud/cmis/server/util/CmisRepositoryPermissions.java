package ru.doccloud.cmis.server.util;

import org.apache.chemistry.opencmis.commons.data.PermissionMapping;

import org.apache.chemistry.opencmis.commons.BasicPermissions;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionMappingDataImpl;

import java.util.*;

public enum CmisRepositoryPermissions {
    CAN_CREATE_DOCUMENT_FOLDER(PermissionMapping.CAN_CREATE_DOCUMENT_FOLDER, BasicPermissions.READ),
    CAN_CREATE_FOLDER_FOLDER(PermissionMapping.CAN_CREATE_FOLDER_FOLDER, BasicPermissions.READ),
    CAN_DELETE_CONTENT_DOCUMENT (PermissionMapping.CAN_DELETE_CONTENT_DOCUMENT, BasicPermissions.WRITE),
    CAN_DELETE_OBJECT(PermissionMapping.CAN_DELETE_OBJECT, BasicPermissions.ALL),
    CAN_DELETE_TREE_FOLDER(PermissionMapping.CAN_DELETE_TREE_FOLDER, BasicPermissions.ALL),
    CAN_GET_ACL_OBJECT(PermissionMapping.CAN_GET_ACL_OBJECT, BasicPermissions.READ),
    CAN_GET_ALL_VERSIONS_VERSION_SERIES(PermissionMapping.CAN_GET_ALL_VERSIONS_VERSION_SERIES, BasicPermissions.READ),
    CAN_GET_CHILDREN_FOLDER(PermissionMapping.CAN_GET_CHILDREN_FOLDER, BasicPermissions.READ),
    CAN_GET_DESCENDENTS_FOLDER(PermissionMapping.CAN_GET_DESCENDENTS_FOLDER, BasicPermissions.READ),
    CAN_GET_FOLDER_PARENT_OBJECT(PermissionMapping.CAN_GET_FOLDER_PARENT_OBJECT, BasicPermissions.READ),
    CAN_GET_PARENTS_FOLDER(PermissionMapping.CAN_GET_PARENTS_FOLDER, BasicPermissions.READ),
    CAN_GET_PROPERTIES_OBJECT(PermissionMapping.CAN_GET_PROPERTIES_OBJECT, BasicPermissions.READ),
    CAN_MOVE_OBJECT(PermissionMapping.CAN_MOVE_OBJECT, BasicPermissions.WRITE),
    CAN_MOVE_SOURCE(PermissionMapping.CAN_MOVE_SOURCE, BasicPermissions.READ),
    CAN_MOVE_TARGET(PermissionMapping.CAN_MOVE_TARGET, BasicPermissions.WRITE),
    CAN_SET_CONTENT_DOCUMENT(PermissionMapping.CAN_SET_CONTENT_DOCUMENT, BasicPermissions.WRITE),
    CAN_UPDATE_PROPERTIES_OBJECT (PermissionMapping.CAN_UPDATE_PROPERTIES_OBJECT, BasicPermissions.WRITE),
    CAN_VIEW_CONTENT_OBJECT (PermissionMapping.CAN_VIEW_CONTENT_OBJECT, BasicPermissions.READ);
    
    private String permissionKey;
    private String permissionValue;

    public String getPermissionKey() {
        return permissionKey;
    }

    public String getPermissionValue() {
        return permissionValue;
    }

    CmisRepositoryPermissions(String permissionKey, String permissionValue) {
        this.permissionKey = permissionKey;
        this.permissionValue = permissionValue;
    }


}
