package ru.doccloud.cmis.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.chemistry.opencmis.commons.impl.ClassLoaderUtil;
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
	private static final Logger LOG = LoggerFactory.getLogger(CmisLifecycleBean.class);
    private ServletContext servletContext;
    private static final String CONFIG_INIT_PARAM = "org.apache.chemistry.opencmis.REPOSITORY_CONFIG_FILE";
    private static final String CONFIG_FILENAME = "/repository.properties";
    private static final String PROPERTY_CLASS = "class";
    
    @Autowired
    private CmisServiceFactory factory;
    

    @Override
    public void setServletContext(ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
    	LOG.info("Factory: "+factory);
    	
        if (factory != null)
        {
        	// get config file name or use default
            String configFilename = servletContext.getInitParameter(CONFIG_INIT_PARAM);
            if (configFilename == null) {
                configFilename = CONFIG_FILENAME;
            }

            
            createServiceFactory(configFilename);
            LOG.info("Factory att : "+CmisRepositoryContextListener.SERVICES_FACTORY);
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
    
    private CmisServiceFactory createServiceFactory(String filename) {
        // load properties
        InputStream stream = this.getClass().getResourceAsStream(filename);

        if (stream == null) {
            LOG.warn("Cannot find configuration!");
            return null;
        }

        Properties props = new Properties();
        try {
            props.load(stream);
        } catch (IOException e) {
            LOG.warn("Cannot load configuration: {}", e.toString(), e);
            return null;
        } finally {
            IOUtils.closeQuietly(stream);
        }

        // get 'class' property
        String className = props.getProperty(PROPERTY_CLASS);
        if (className == null) {
            LOG.warn("Configuration doesn't contain the property 'class'!");
            return null;
        }

        // initialize factory instance
        Map<String, String> parameters = new HashMap<String, String>();

        for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            String value = props.getProperty(key);
            parameters.put(key, value);
        }

        factory.init(parameters);

        if (LOG.isInfoEnabled()) {
            LOG.info("Initialized Services Factory: {}", factory.getClass().getName());
        }

        return factory;
    }
}