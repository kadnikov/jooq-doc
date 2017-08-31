package ru.doccloud.webapp;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.catalina.Context;
import org.apache.catalina.realm.JDBCRealm;
import org.apache.catalina.startup.Tomcat;
import org.apache.chemistry.opencmis.server.impl.atompub.CmisAtomPubServlet;
import org.apache.chemistry.opencmis.server.impl.webservices.CmisWebServicesServlet;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;

import ru.doccloud.cmis.server.MyCmisBrowserBindingServlet;

@Configuration
@ComponentScan({
                "ru.doccloud.config"
})
@SpringBootApplication
public class WebApplication extends SpringBootServletInitializer implements WebApplicationInitializer {


    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    	return application.sources(WebApplication.class);
    }


    private static final Logger LOGGER = LoggerFactory.getLogger(WebApplication.class);

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
				final ContextResource resource = new ContextResource();

                LOGGER.info("entering postProcessContext(context={})", context);

				resource.setName("jdbc/DOCCLOUDDB");
				resource.setType(DataSource.class.getName());
                resource.setProperty("factory", "org.apache.tomcat.jdbc.pool.DataSourceFactory");
				resource.setProperty("driverClassName", "org.postgresql.Driver");
				resource.setProperty("url", "jdbc:postgresql://localhost:5432/doccloud");

                resource.setProperty("maxTotal", "100");
                resource.setProperty("maxIdle", "20");
                resource.setProperty("minIdle", "5");
                resource.setProperty("maxWaitMillis", "10000");


                resource.setProperty("username", "doccloud");
                resource.setProperty("password", "doccloud");

                resource.setAuth("Container");

				context.getNamingResources().addResource(resource);


                LOGGER.info("postProcessContext(): creating realm");
                final JDBCRealm realm = new JDBCRealm();

                realm.setDriverName("org.postgresql.Driver");
                realm.setConnectionURL("jdbc:postgresql://postgres:5432/doccloud");
                realm.setConnectionName("pupkin");
                realm.setConnectionPassword("pupkin");
                realm.setUserTable("users");
                realm.setUserNameCol("userid");
                realm.setUserCredCol("password");
                realm.setUserRoleTable("user_roles");
                realm.setRoleNameCol("role");
                realm.setAllRolesMode("authOnly");
				context.setRealm(realm);
				
				LoginConfig config = new LoginConfig();
                config.setAuthMethod("BASIC");
                context.setLoginConfig(config);
                context.addSecurityRole("tomcat");

                SecurityConstraint constraint = new SecurityConstraint();
                constraint.addAuthRole("tomcat");

                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                constraint.addCollection(collection);

                context.addConstraint(constraint);

                LOGGER.info("leaving postProcessContext(): context={}", context.getRealm());
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
}