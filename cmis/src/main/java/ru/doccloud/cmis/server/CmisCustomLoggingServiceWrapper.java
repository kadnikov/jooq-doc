package ru.doccloud.cmis.server;

import java.math.BigInteger;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.support.wrapper.AbstractCmisServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example of a minimal CMIS Custom Service Wrapper (logging example)
 * 
 * Add the following ** to the repository.properties to have framework hook into
 * chain. The number at the key is the position in the wrapper stack. Lower
 * numbers are outer wrappers, higher numbers are inner wrappers.
 *
 * ** add the following line to your repository.properties file in your servers
 * war:
 * 
 * <pre>
 * servicewrapper.1=org.foo.CmisCustomLoggingServiceWrapper
 * </pre>
 */
public class CmisCustomLoggingServiceWrapper extends AbstractCmisServiceWrapper {

    // slf4j example
    private static final Logger LOGGER = LoggerFactory.getLogger(CmisCustomLoggingServiceWrapper.class);

    // provide constructor
    public CmisCustomLoggingServiceWrapper(CmisService service) {
        super(service);

    }

    /**
     * slf logging version with dual output to console and slf
     */
    private void slflog(String repositoryId) {
        if (repositoryId == null) {
            repositoryId = "<none>";
        }

        HttpServletRequest request = (HttpServletRequest) getCallContext().get(CallContext.HTTP_SERVLET_REQUEST);
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "<unknown>";
        }

        String binding = getCallContext().getBinding();

        LOGGER.debug("Operation: {}, Repository ID: {}, Binding: {}, User Agent: {}", "getChildren ", repositoryId, binding,
                userAgent);
    }

    @Override
    public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {

        if(LOGGER.isDebugEnabled()) {
            slflog(repositoryId);
        }
        long startTime = System.currentTimeMillis();

        ObjectInFolderList retVal = getWrappedService().getChildren(repositoryId, folderId, filter, orderBy,
                includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, maxItems,
                skipCount, extension);

        // dual log output in case logger not configured
        LOGGER.debug("[CmisCustomServiceWrapper] Exiting method getChildren. time (ms):"
                + (System.currentTimeMillis() - startTime));
        return retVal;
    }

}
