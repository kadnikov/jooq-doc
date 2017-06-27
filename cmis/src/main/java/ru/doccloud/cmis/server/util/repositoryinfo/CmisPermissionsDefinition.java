package ru.doccloud.cmis.server.util.repositoryinfo;

import org.apache.chemistry.opencmis.commons.BasicPermissions;


public enum CmisPermissionsDefinition {
    READ(BasicPermissions.READ, "Read"),
    WRITE(BasicPermissions.WRITE, "Write"),
    ALL(BasicPermissions.ALL, "All");

    private String definitionKey;
    private String getDefinitionValue;

    CmisPermissionsDefinition(String definitionKey, String getDefinitionValue) {
        this.definitionKey = definitionKey;
        this.getDefinitionValue = getDefinitionValue;
    }

    public String getDefinitionKey() {
        return definitionKey;
    }

    public String getGetDefinitionValue() {
        return getDefinitionValue;
    }
}
