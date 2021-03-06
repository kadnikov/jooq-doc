package ru.doccloud.config;

import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.doccloud.config.exception.JOOQToSpringExceptionTransformer;

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

    private static final String DB_SCHEMA_SCRIPT = "schema.sql";

    private static final String DATASOURCE_JNDI_NAME = "jdbc/DOCCLOUDDB";

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceContext.class);

    @Autowired
    private Environment env;


    @Bean(destroyMethod = "")
    public DataSource dataSource() throws Exception {
        try {
            JndiDataSourceLookup dataSource = new JndiDataSourceLookup();
            dataSource.setResourceRef(true);
            final DataSource ds = dataSource.getDataSource(DATASOURCE_JNDI_NAME);

            LOGGER.info("dataSource(): {}", ds);
            if (ds == null)
                throw new Exception("Datasource with jndi " + DATASOURCE_JNDI_NAME + " was not found. Please create datasource");
            LOGGER.trace("datasource {}", ds);
            return ds;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @Bean
    public DataSourceTransactionManager transactionManager() throws Exception {
        return new DataSourceTransactionManager(new LazyConnectionDataSourceProxy(dataSource()));
    }

    @Bean
    public DataSourceConnectionProvider connectionProvider() throws Exception {
        return new DataSourceConnectionProvider(new TransactionAwareDataSourceProxy(new LazyConnectionDataSourceProxy(dataSource())));
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

        jooqConfiguration.set(SQLDialect.POSTGRES);

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
                new ClassPathResource(DB_SCHEMA_SCRIPT)
        );

        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
