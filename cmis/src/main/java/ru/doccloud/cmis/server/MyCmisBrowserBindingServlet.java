package ru.doccloud.cmis.server;

import org.apache.chemistry.opencmis.commons.exceptions.*;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.impl.browser.AbstractBrowserServiceCall;
import org.apache.chemistry.opencmis.server.impl.browser.BrowserCallContextImpl;
import org.apache.chemistry.opencmis.server.impl.browser.CmisBrowserBindingServlet;
import org.apache.chemistry.opencmis.server.shared.Dispatcher;
import org.apache.chemistry.opencmis.server.shared.ExceptionHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static org.apache.chemistry.opencmis.commons.impl.JSONConstants.*;

public class MyCmisBrowserBindingServlet extends CmisBrowserBindingServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(MyCmisBrowserBindingServlet.class.getName());

    private static final MyBrowserServiceCall ERROR_SERTVICE_CALL = new MyBrowserServiceCall();
    
    @Override
    public void init(ServletConfig config) throws ServletException {
    	LOGGER.debug("init(config={})", config);
        super.init(config);
    }
    
	 @Override
	    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	            IOException {
			 LOGGER.debug(" service(): MyCmis Service CALLED");
			 try {
                 String host = request.getHeader("x-forwarded-host") != null ? request.getHeader("x-forwarded-host")
                         : request.getHeader("host");


                 String baseUrl = "http://" + host;

                 baseUrl = StringUtils.stripEnd(baseUrl, "/") + request.getContextPath()  + request.getServletPath() + "/"
                         + AbstractBrowserServiceCall.REPOSITORY_PLACEHOLDER + "/";

                 LOGGER.debug("service(): baseUrl {}", baseUrl);

                 request.setAttribute(Dispatcher.BASE_URL_ATTRIBUTE, baseUrl);

				 super.service(request, response);
			 } catch (Exception e) {
			 	LOGGER.error("Exception {}",e.getMessage());
			 	e.printStackTrace();
			 }
	 
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
	                LOGGER.error(createLogMessage(ex, request), ex);
	                statusCode = getErrorCode((CmisRuntimeException) ex);
	            } else if (ex instanceof CmisStorageException) {
	                LOGGER.error(createLogMessage(ex, request), ex);
	                statusCode = getErrorCode((CmisStorageException) ex);
	                exceptionName = ((CmisStorageException) ex).getExceptionName();
	            } else if (ex instanceof CmisBaseException) {
	                statusCode = getErrorCode((CmisBaseException) ex);
	                exceptionName = ((CmisBaseException) ex).getExceptionName();

	                if (statusCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
	                    LOGGER.error(createLogMessage(ex, request), ex);
	                }
	            } else if (ex instanceof IOException) {
	                LOGGER.warn(createLogMessage(ex, request), ex);
	            } else {
	                LOGGER.error(createLogMessage(ex, request), ex);
	            }

	            if (response.isCommitted()) {
	                LOGGER.warn("Failed to send error message to client. Response is already committed.", ex);
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
	                    LOGGER.error(createLogMessage(ex, request), e);
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
