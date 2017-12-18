package ru.doccloud.cmis.server;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.server.impl.atompub.CmisAtomPubServlet;
import org.apache.chemistry.opencmis.server.impl.browser.AbstractBrowserServiceCall;
import org.apache.chemistry.opencmis.server.shared.Dispatcher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyCmisAtomPubServlet extends CmisAtomPubServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyCmisAtomPubServlet.class.getName());

    
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
}
