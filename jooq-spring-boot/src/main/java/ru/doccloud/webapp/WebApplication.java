package ru.doccloud.webapp;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

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