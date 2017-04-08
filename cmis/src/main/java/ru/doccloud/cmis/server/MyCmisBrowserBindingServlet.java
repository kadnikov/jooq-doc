package ru.doccloud.cmis.server;

import static org.apache.chemistry.opencmis.commons.impl.JSONConstants.ERROR_EXCEPTION;
import static org.apache.chemistry.opencmis.commons.impl.JSONConstants.ERROR_MESSAGE;
import static org.apache.chemistry.opencmis.commons.impl.JSONConstants.ERROR_STACKTRACE;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisFilterNotValidException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisServiceUnavailableException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisTooManyRequestsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.impl.browser.AbstractBrowserServiceCall;
import org.apache.chemistry.opencmis.server.impl.browser.BrowserCallContextImpl;
import org.apache.chemistry.opencmis.server.impl.browser.CmisBrowserBindingServlet;
import org.apache.chemistry.opencmis.server.shared.Dispatcher;
import org.apache.chemistry.opencmis.server.shared.ExceptionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyCmisBrowserBindingServlet extends CmisBrowserBindingServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(MyCmisBrowserBindingServlet.class.getName());

    private static final MyBrowserServiceCall ERROR_SERTVICE_CALL = new MyBrowserServiceCall();
    
    @Override
    public void init(ServletConfig config) throws ServletException {
    	LOG.debug("MyCmis INIT CALLED");
        super.init(config);
    }
    
	 @Override
	    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	            IOException {
			 LOG.debug("MyCmis Service CALLED");
//			 LOG.debug("x-forwarded-proto header - "+request.getHeader("x-forwarded-proto"));
//			 LOG.debug("getServerName - "+request.getServerName());
//			 LOG.debug("getServerPort - "+request.getServerPort());
//			 LOG.debug("host header - "+request.getHeader("host"));
//			 Enumeration<String> headerNames = request.getHeaderNames();
//
//		        if (headerNames != null) {
//		                while (headerNames.hasMoreElements()) {
//					String headerName = headerNames.nextElement();
//		                        LOG.debug("Header: "+ headerName+ " - " + request.getHeader(headerName));
//		                }
//		        }
        String host = request.getHeader("host");
        if (request.getHeader("x-forwarded-host")!=null){
        	host = request.getHeader("x-forwarded-host");
        }
		 request.setAttribute(Dispatcher.BASE_URL_ATTRIBUTE, "http://"+host+"/jooq/browser");
		 super.service(request, response);
	 
	 }
	 
	    /**
	     * Translates an exception in an appropriate HTTP error code.
	     */
	    protected int getErrorCode(CmisBaseException ex) {
	        return ERROR_SERTVICE_CALL.getErrorCode(ex);
	    }

	    /**
	     * Prints an error as JSON.
	     */
	    protected void printError(CallContext context, Exception ex, HttpServletRequest request,
	            HttpServletResponse response) {
	        ERROR_SERTVICE_CALL.printError(context, ex, request, response);
	    }
	 
	// this class exists in order to call AbstractBrowserServiceCall methods
	    public static class MyBrowserServiceCall extends AbstractBrowserServiceCall {
	        @Override
	        public void serve(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
	                HttpServletResponse response) {
	            // no implementation
	        }
	        
	        public int getErrorCode(CmisBaseException ex) {
	            if (ex instanceof CmisConstraintException) {
	                return 409;
	            } else if (ex instanceof CmisContentAlreadyExistsException) {
	                return 409;
	            } else if (ex instanceof CmisFilterNotValidException) {
	                return 400;
	            } else if (ex instanceof CmisInvalidArgumentException) {
	                return 400;
	            } else if (ex instanceof CmisNameConstraintViolationException) {
	                return 409;
	            } else if (ex instanceof CmisNotSupportedException) {
	                return 405;
	            } else if (ex instanceof CmisObjectNotFoundException) {
	                return 404;
	            } else if (ex instanceof CmisPermissionDeniedException) {
	                return 403;
	            } else if (ex instanceof CmisStorageException) {
	                return 500;
	            } else if (ex instanceof CmisStreamNotSupportedException) {
	                return 403;
	            } else if (ex instanceof CmisUpdateConflictException) {
	                return 409;
	            } else if (ex instanceof CmisVersioningException) {
	                return 409;
	            } else if (ex instanceof CmisTooManyRequestsException) {
	                return 429;
	            } else if (ex instanceof CmisServiceUnavailableException) {
	                return 503;
	            }

	            return 500;
	        }

	        public void printError(CallContext context, Exception ex, HttpServletRequest request,
	                HttpServletResponse response) {
	            int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	            String exceptionName = CmisRuntimeException.EXCEPTION_NAME;

	            if (ex instanceof CmisRuntimeException) {
	                LOG.error(createLogMessage(ex, request), ex);
	                statusCode = getErrorCode((CmisRuntimeException) ex);
	            } else if (ex instanceof CmisStorageException) {
	                LOG.error(createLogMessage(ex, request), ex);
	                statusCode = getErrorCode((CmisStorageException) ex);
	                exceptionName = ((CmisStorageException) ex).getExceptionName();
	            } else if (ex instanceof CmisBaseException) {
	                statusCode = getErrorCode((CmisBaseException) ex);
	                exceptionName = ((CmisBaseException) ex).getExceptionName();

	                if (statusCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
	                    LOG.error(createLogMessage(ex, request), ex);
	                }
	            } else if (ex instanceof IOException) {
	                LOG.warn(createLogMessage(ex, request), ex);
	            } else {
	                LOG.error(createLogMessage(ex, request), ex);
	            }

	            if (response.isCommitted()) {
	                LOG.warn("Failed to send error message to client. Response is already committed.", ex);
	                return;
	            }

	            String token = (context instanceof BrowserCallContextImpl ? ((BrowserCallContextImpl) context).getToken()
	                    : null);

	            String message = ex.getMessage();
	            if (!(ex instanceof CmisBaseException)) {
	                message = "An error occurred!";
	            }

	            if (token == null) {
	                response.resetBuffer();
	                setStatus(request, response, statusCode);

	                JSONObject jsonResponse = new JSONObject();

	                jsonResponse.put(ERROR_EXCEPTION, exceptionName);
	                jsonResponse.put(ERROR_MESSAGE, message);

	                String st = ExceptionHelper.getStacktraceAsString(ex);
	                if (st != null) {
	                    jsonResponse.put(ERROR_STACKTRACE, st);
	                }

	                if (ex instanceof CmisBaseException) {
	                    Map<String, String> additionalData = ((CmisBaseException) ex).getAdditionalData();
	                    if (additionalData != null && !additionalData.isEmpty()) {
	                        for (Map.Entry<String, String> e : additionalData.entrySet()) {
	                            if (ERROR_EXCEPTION.equalsIgnoreCase(e.getKey())
	                                    || ERROR_MESSAGE.equalsIgnoreCase(e.getKey())) {
	                                continue;
	                            }
	                            jsonResponse.put(e.getKey(), e.getValue());
	                        }
	                    }
	                }

	                try {
	                    writeJSON(jsonResponse, request, response);
	                } catch (Exception e) {
	                    LOG.error(createLogMessage(ex, request), e);
	                    try {
	                        response.sendError(statusCode, message);
	                    } catch (Exception en) {
	                        // there is nothing else we can do
	                    }
	                }
	            } else {
	                setStatus(request, response, HttpServletResponse.SC_OK);
	                response.setContentType(HTML_MIME_TYPE);
	                response.setContentLength(0);

	                if (context != null) {
	                    setCookie(request, response, context.getRepositoryId(), token,
	                            createCookieValue(statusCode, null, exceptionName, message));
	                }
	            }
	        }
	    }

}
