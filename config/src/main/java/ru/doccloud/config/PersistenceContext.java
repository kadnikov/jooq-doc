package ru.doccloud.config;

import com.jolbox.bonecp.BoneCPDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import ru.doccloud.config.exception.JOOQToSpringExceptionTransformer;

import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * @author Andrey Kadnikov
 */
@Configuration
@ComponentScan({
        "ru.doccloud.common.service",
        "ru.doccloud.document.repository"
})
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
public class PersistenceContext {

    private static final String PROPERTY_NAME_DB_DRIVER = "db.driver";
    private static final String PROPERTY_NAME_DB_PASSWORD = "db.password";
    private static final String PROPERTY_NAME_DB_SCHEMA_SCRIPT = "db.schema.script";
    private static final String PROPERTY_NAME_DB_URL = "db.url";
    private static final String PROPERTY_NAME_DB_USERNAME = "db.username";
    private static final String PROPERTY_NAME_JOOQ_SQL_DIALECT = "jooq.sql.dialect";

    private static final String DATASOURCE_JNDI_NAME = "jdbc/DOCCLOUDDB";

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceContext.class);

    @Autowired
    private Environment env;

    @Bean(destroyMethod = "close")
    public DataSource dataSource() throws Exception {
//        Context ctx = new InitialContext();
//
//        DataSource ds = (DataSource)ctx.lookup(DATASOURCE_JNDI_NAME);
        JndiDataSourceLookup dataSource = new JndiDataSourceLookup();
        dataSource.setResourceRef(true);
        DataSource ds =  dataSource.getDataSource(DATASOURCE_JNDI_NAME);
        if(ds == null)
            throw new Exception("Datasource with jndi " + DATASOURCE_JNDI_NAME + " was not found. Please create datasource");
        LOGGER.trace("datasource " + ds) ;
        return ds;
    }

    @Bean
    public LazyConnectionDataSourceProxy lazyConnectionDataSource() throws Exception {
        return new LazyConnectionDataSourceProxy(dataSource());
    }

    @Bean
    public TransactionAwareDataSourceProxy transactionAwareDataSource() throws Exception {
        return new TransactionAwareDataSourceProxy(lazyConnectionDataSource());
    }

    @Bean
    public DataSourceTransactionManager transactionManager() throws Exception {
        return new DataSourceTransactionManager(lazyConnectionDataSource());
    }

    @Bean
    public DataSourceConnectionProvider connectionProvider() throws Exception {
        return new DataSourceConnectionProvider(transactionAwareDataSource());
    }

    @Bean
    public JOOQToSpringExceptionTransformer jooqToSpringExceptionTransformer() {
        return new JOOQToSpringExceptionTransformer();
    }

    @Bean
    public DefaultConfiguration configuration() throws Exception {
        DefaultConfiguration jooqConfiguration = new DefaultConfiguration();

        jooqConfiguration.set(connectionProvider());
        jooqConfiguration.set(new DefaultExecuteListenerProvider(
            jooqToSpringExceptionTransformer()
        ));

        String sqlDialectName = env.getRequiredProperty(PROPERTY_NAME_JOOQ_SQL_DIALECT);
        SQLDialect dialect = SQLDialect.valueOf(sqlDialectName);
        jooqConfiguration.set(dialect);

        return jooqConfiguration;
    }

    @Bean
    public DefaultDSLContext dsl() throws Exception {
        return new DefaultDSLContext(configuration());
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer() throws Exception {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource());

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(
                new ClassPathResource(env.getRequiredProperty(PROPERTY_NAME_DB_SCHEMA_SCRIPT))
        );

        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
