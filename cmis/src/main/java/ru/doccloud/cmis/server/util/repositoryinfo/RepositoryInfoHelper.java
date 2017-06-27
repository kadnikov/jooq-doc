package ru.doccloud.cmis.server.util.repositoryinfo;

import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.enums.*;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;

import java.lang.reflect.Field;
import java.util.*;

public enum  RepositoryInfoHelper {
    INSTANCE;

    public AclCapabilitiesDataImpl getAclCapability() {
        AclCapabilitiesDataImpl aclCapability = new AclCapabilitiesDataImpl();
        aclCapability.setSupportedPermissions(SupportedPermissions.BASIC);
        aclCapability.setAclPropagation(AclPropagation.OBJECTONLY);
        aclCapability.setPermissionDefinitionData(getPermissionDefinitions());
        aclCapability.setPermissionMappingData(getPermissionMappingMap());
        return aclCapability;
    }

    public NewTypeSettableAttributesImpl getSettableAttributes() throws IllegalAccessException {
        NewTypeSettableAttributesImpl typeSetAttributes = new NewTypeSettableAttributesImpl();
        Field[] fields = typeSetAttributes.getClass().getDeclaredFields();
        for(Field field: fields){
            if(field.getType().equals(boolean.class)) {
                field.setAccessible(true);
                field.set(typeSetAttributes, false);
            }
        }
        return typeSetAttributes;
    }
    public RepositoryCapabilitiesImpl getRepositoryCapabilitiesImpl(){
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
        return capabilities;
    }

    private Map<String, PermissionMapping> getPermissionMappingMap() {
        Map<String, PermissionMapping> permissionMappingMap = new LinkedHashMap<>();
        for (PermissionMapping pm : getPermissionMappingList()) {
            permissionMappingMap.put(pm.getKey(), pm);
        }
        return permissionMappingMap;
    }

    private List<PermissionDefinition> getPermissionDefinitions(){
        List<PermissionDefinition> permissions = new ArrayList<>();
        for(CmisPermissionsDefinition definition: CmisPermissionsDefinition.values()){
            permissions.add(createPermission(definition.getDefinitionKey(), definition.getGetDefinitionValue()));
        }
        return permissions;
    }

    private List<PermissionMapping> getPermissionMappingList() {
        List<PermissionMapping> permissionMappingList = new ArrayList<>();
        for(CmisRepositoryPermissions permission: CmisRepositoryPermissions.values()){
            permissionMappingList.add(createMapping(permission.getPermissionKey(), permission.getPermissionValue()));
        }
        return permissionMappingList;
    }

    private static PermissionMapping createMapping(String key, String permission) {
        PermissionMappingDataImpl pm = new PermissionMappingDataImpl();
        pm.setKey(key);
        pm.setPermissions(Collections.singletonList(permission));

        return pm;
    }

    private static PermissionDefinition createPermission(String permission, String description) {
        PermissionDefinitionDataImpl pd = new PermissionDefinitionDataImpl();
        pd.setId(permission);
        pd.setDescription(description);

        return pd;
    }
}
