package ru.doccloud.cmis.server;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.impl.browser.AbstractBrowserServiceCall;
import org.apache.chemistry.opencmis.server.impl.browser.CmisBrowserBindingServlet;
import org.apache.chemistry.opencmis.server.shared.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyCmisBrowserBindingServlet extends CmisBrowserBindingServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(MyCmisBrowserBindingServlet.class.getName());

	
	 @Override
	    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	            IOException {
			 LOG.debug("LOGIN CALLED");
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
	 
	// this class exists in order to call AbstractBrowserServiceCall methods
	    public static class MyBrowserServiceCall extends AbstractBrowserServiceCall {
	        @Override
	        public void serve(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
	                HttpServletResponse response) {
	            // no implementation
	        }
	    }

}
