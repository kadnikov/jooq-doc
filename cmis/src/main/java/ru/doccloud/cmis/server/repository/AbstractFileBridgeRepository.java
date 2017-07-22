package ru.doccloud.cmis.server.repository;


import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.CapabilityOrderBy;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.doccloud.cmis.server.FileBridgeTypeManager;
import ru.doccloud.cmis.server.util.repositoryinfo.RepositoryInfoHelper;

import java.io.File;

public abstract class AbstractFileBridgeRepository extends BridgeRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFileBridgeRepository.class);
    /** Repository id. */
    protected final String repositoryId;

    private RepositoryInfo repositoryInfo;

    AbstractFileBridgeRepository(String repositoryId, FileBridgeTypeManager typeManager, String rootPath) {
        super(rootPath, typeManager);

        // check repository id
        if (StringUtils.isBlank(repositoryId)) {
            throw new IllegalArgumentException("Invalid repository id!");
        }

        this.repositoryId = repositoryId;
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
//    todo get root from database from document with id =0
    File getRootDirectory() {
//        return root;
        return null;
    }

    private RepositoryInfo createRepositoryInfo(CmisVersion cmisVersion) throws IllegalAccessException {

        LOGGER.trace("entering createRepositoryInfo(cmisVersion={})", cmisVersion);

        RepositoryInfoImpl repositoryInfo = new RepositoryInfoImpl();
        if(cmisVersion == null)
            throw new IllegalArgumentException("createRepositoryInfo(): cmisVersion is null");

        repositoryInfo.setId(repositoryId);
        repositoryInfo.setName(repositoryId);
        repositoryInfo.setDescription(repositoryId);

        repositoryInfo.setCmisVersionSupported(cmisVersion.value());

        repositoryInfo.setProductName("DocCloud Server");
        repositoryInfo.setProductVersion("1.0");
        repositoryInfo.setVendorName("DocCloud");

        repositoryInfo.setRootFolder(ROOT_ID);

        repositoryInfo.setThinClientUri("");
        repositoryInfo.setChangesIncomplete(true);

        RepositoryCapabilitiesImpl capabilities = RepositoryInfoHelper.INSTANCE.getRepositoryCapabilitiesImpl();
        LOGGER.trace("createRepositoryInfo(): capabilities {}", capabilities);
        if (cmisVersion != CmisVersion.CMIS_1_0) {
            capabilities.setCapabilityOrderBy(CapabilityOrderBy.NONE);

            NewTypeSettableAttributesImpl typeSetAttributes = RepositoryInfoHelper.INSTANCE.getSettableAttributes();
            LOGGER.trace("createRepositoryInfo(): typeSetAttributes {}", typeSetAttributes);
            capabilities.setNewTypeSettableAttributes(typeSetAttributes);

            CreatablePropertyTypesImpl creatablePropertyTypes = new CreatablePropertyTypesImpl();
            capabilities.setCreatablePropertyTypes(creatablePropertyTypes);
        }

        repositoryInfo.setCapabilities(capabilities);

        AclCapabilitiesDataImpl aclCapability = RepositoryInfoHelper.INSTANCE.getAclCapability();
        LOGGER.trace("createRepositoryInfo(): aclCapability {}", aclCapability);
        repositoryInfo.setAclCapabilities(aclCapability);
        LOGGER.trace("leaving createRepositoryInfo(): repositoryInfo {}", repositoryInfo);
        return repositoryInfo;
    }

    /**
     * CMIS getRepositoryInfo.
     */
    public RepositoryInfo getRepositoryInfo(CallContext context) throws IllegalAccessException {
        checkUser(context, false);
        LOGGER.trace("getRepositoryInfo(): cmisVersion {})", context.getCmisVersion());
        if(repositoryInfo == null)
            repositoryInfo = createRepositoryInfo(context.getCmisVersion());
        return repositoryInfo;
    }
}

