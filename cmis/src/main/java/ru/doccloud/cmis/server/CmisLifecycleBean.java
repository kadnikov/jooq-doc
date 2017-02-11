package ru.doccloud.cmis.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.server.impl.CmisRepositoryContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

@Component
public class CmisLifecycleBean implements ServletContextAware,InitializingBean, DisposableBean

{
	private static final Logger LOGGER = LoggerFactory.getLogger(CmisLifecycleBean.class);
    private ServletContext servletContext;
    private static final String CONFIG_INIT_PARAM = "org.apache.chemistry.opencmis.REPOSITORY_CONFIG_FILE";
    private static final String CONFIG_FILENAME = "/repository.properties";
    private static final String PROPERTY_CLASS = "class";
    
    private final CmisServiceFactory factory;

    @Autowired
    public CmisLifecycleBean(CmisServiceFactory factory) {
        this.factory = factory;
    }


    @Override
    public void setServletContext(ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
    	LOGGER.debug("Factory: "+factory);
    	
        if (factory != null)
        {
        	// get config file name or use default
            String configFilename = servletContext.getInitParameter(CONFIG_INIT_PARAM);
            if (configFilename == null) {
                configFilename = CONFIG_FILENAME;
            }

            
            createServiceFactory(configFilename);
            LOGGER.info("Factory att : "+CmisRepositoryContextListener.SERVICES_FACTORY);
            servletContext.setAttribute(CmisRepositoryContextListener.SERVICES_FACTORY, factory);
        }
    }

    @Override
    public void destroy() throws Exception
    {
        if (factory != null)
        {
            factory.destroy();
        }
    }
    
    private CmisServiceFactory createServiceFactory(final String filename) {
        // load properties
        InputStream stream = this.getClass().getResourceAsStream(filename);

        if (stream == null) {
            LOGGER.warn("Cannot find configuration!");
            return null;
        }

        Properties props = new Properties();
        try {
            props.load(stream);
        } catch (IOException e) {
            LOGGER.warn("Cannot load configuration: {}", e.toString(), e);
            return null;
        } finally {
            IOUtils.closeQuietly(stream);
        }

        // get 'class' property
        final String className = props.getProperty(PROPERTY_CLASS);
        if (className == null) {
            LOGGER.warn("Configuration doesn't contain the property 'class'!");
            return null;
        }

        // initialize factory instance
        Map<String, String> parameters = new HashMap<String, String>();

        for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
            final String key = (String) e.nextElement();
            final String value = props.getProperty(key);
            parameters.put(key, value);
        }

        factory.init(parameters);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Initialized Services Factory: {}", factory.getClass().getName());
        }

        return factory;
    }
}