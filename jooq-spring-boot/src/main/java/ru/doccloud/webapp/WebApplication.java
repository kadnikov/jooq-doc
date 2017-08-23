package ru.doccloud.webapp;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.chemistry.opencmis.server.impl.atompub.CmisAtomPubServlet;
import org.apache.chemistry.opencmis.server.impl.webservices.CmisWebServicesServlet;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.doccloud.cmis.server.MyCmisBrowserBindingServlet;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
//@EnableAutoConfiguration
//@ComponentScan
@ComponentScan({
                "ru.doccloud.config"
})
//@ConditionalOnProperty(prefix = "spring.datasource", name = "jndi-name")
//@ImportResource(value = {
//        "classpath:context.xml",
//                        "classpath:server.xml",
//                        "classpath:tomcat-users.xml"})
@SpringBootApplication
public class WebApplication extends SpringBootServletInitializer {

//	@Override
//	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//		return application.sources(WebApplication.class);
//	}
//
//	public static void main(String[] args) throws Exception {
//		SpringApplication.run(WebApplication.class, args);
//	}

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder()
                .sources(WebApplication.class)
                .run(args);
    }

	@Bean
	public TomcatEmbeddedServletContainerFactory tomcatFactory() {
		return new TomcatEmbeddedServletContainerFactory() {

			@Override
			protected TomcatEmbeddedServletContainer getTomcatEmbeddedServletContainer(
					Tomcat tomcat) {
				tomcat.enableNaming();
				return super.getTomcatEmbeddedServletContainer(tomcat);
			}

			@Override
			protected void postProcessContext(Context context) {
				ContextResource resource = new ContextResource();
				resource.setName("jdbc/DOCCLOUDDB");
				resource.setType(DataSource.class.getName());
                resource.setProperty("factory", "org.apache.tomcat.jdbc.pool.DataSourceFactory");
				resource.setProperty("driverClassName", "org.postgresql.Driver");
				resource.setProperty("url", "jdbc:postgresql://localhost:5432/doccloud");
                resource.setProperty("username", "pupkin");
                resource.setProperty("password", "pupkin");

				context.getNamingResources().addResource(resource);

//                ContextResourceLink resourceLink = new ContextResourceLink();
//                resourceLink.setName("jdbc/DOCCLOUDDB");
//                resourceLink.setGlobal("jdbc/DOCCLOUDDB");
//                resourceLink.setType("javax.sql.DataSource");
//                resourceLink.setProperty("auth", "Container");
//
//                context.getNamingResources().addResourceLink(resourceLink);
			}
		};
	}


    @Bean
    public CmisWebServicesServlet cmis10WebServiceServlet(){
        return new CmisWebServicesServlet();
    }

    @Bean
    public ServletRegistrationBean cmisws10() {
        ServletRegistrationBean registration = new ServletRegistrationBean(cmis10WebServiceServlet(), "/services/*");
        Map<String,String> params = new HashMap<>();
        params.put("cmisVersion","1.0");
        registration.setInitParameters(params);
        return registration;
    }

    @Bean
    public CmisWebServicesServlet cmis11WebServiceServlet(){
        return new CmisWebServicesServlet();
    }

    @Bean
    public ServletRegistrationBean cmisws11() {
        ServletRegistrationBean registration = new ServletRegistrationBean(cmis11WebServiceServlet(), "/services11/*");
        Map<String,String> params = new HashMap<>();
        params.put("cmisVersion","1.1");
        registration.setInitParameters(params);
        return registration;
    }


    @Bean
    public CmisAtomPubServlet cmis10AtomPubServlet(){
        return new CmisAtomPubServlet();
    }

    @Bean
    public ServletRegistrationBean cmisatom10() {
        ServletRegistrationBean registration = new ServletRegistrationBean(cmis10AtomPubServlet(), "/atom/*");
        Map<String,String> params = new HashMap<>();
        params.put("callContextHandler","org.apache.chemistry.opencmis.server.impl.browser.token.TokenCallContextHandler");
        params.put("cmisVersion","1.0");
        registration.setInitParameters(params);
        return registration;
    }

    @Bean
    public CmisAtomPubServlet cmis11AtomPubServlet(){
        return new CmisAtomPubServlet();
    }

    @Bean
    public ServletRegistrationBean cmisatom11() {
        ServletRegistrationBean registration = new ServletRegistrationBean(cmis11AtomPubServlet(), "/atom11/*");
        Map<String,String> params = new HashMap<>();
        params.put("callContextHandler","org.apache.chemistry.opencmis.server.impl.browser.token.TokenCallContextHandler");
        params.put("cmisVersion","1.1");
        registration.setInitParameters(params);
        return registration;
    }

    @Bean
    public MyCmisBrowserBindingServlet cmisbrowserServlet(){
        return new MyCmisBrowserBindingServlet();
    }

    @Bean
    public ServletRegistrationBean cmisbrowser() {
        ServletRegistrationBean registration = new ServletRegistrationBean(cmisbrowserServlet(), "/browser/*");
        Map<String,String> params = new HashMap<>();
        params.put("callContextHandler","org.apache.chemistry.opencmis.server.impl.browser.token.TokenCallContextHandler");
        registration.setInitParameters(params);
        return registration;
    }





//    @Bean(destroyMethod="")
//    public DataSource jndiDataSource() throws IllegalArgumentException, NamingException {
//        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
//        bean.setJndiName("java:comp/env/jdbc/DOCCLOUDDB");
//        bean.setProxyInterface(DataSource.class);
//        bean.setLookupOnStartup(false);
//        bean.afterPropertiesSet();
//        return (DataSource)bean.getObject();
//    }

//    @Bean(destroyMethod = "")
//    @ConditionalOnMissingBean
//    public DataSource dataSource(DataSourceProperties properties) {
//        JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
//        DataSource dataSource = dataSourceLookup.getDataSource(properties.getJndiName());
//        return dataSource;
//    }

//	@Bean
//	public TomcatEmbeddedServletContainerFactory tomcatFactory() {
//		return new TomcatEmbeddedServletContainerFactory() {
//
//			@Override
//			protected TomcatEmbeddedServletContainer getTomcatEmbeddedServletContainer(
//					Tomcat tomcat) {
//				tomcat.enableNaming();
//				return super.getTomcatEmbeddedServletContainer(tomcat);
//			}
//		};
//	}

}