package ru.doccloud.document;

import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import com.jolbox.bonecp.BoneCPDataSource;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
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
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.doccloud.document.dbfactory.DoccloudPostgresqlDataTypeFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Types;

@Configuration
@ComponentScan({
        "ru.doccloud.document",
        "ru.doccloud.repository",
        "ru.doccloud.common"
})
@EnableTransactionManagement
@PropertySource("application_it_test.properties")
public class PersistenceContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceContext.class);

    private static final String PROPERTY_NAME_DB_DRIVER = "db.driver";
    private static final String PROPERTY_NAME_DB_PASSWORD = "db.password";
    private static final String PROPERTY_NAME_DB_SCHEMA_SCRIPT = "db.schema.script";
    private static final String PROPERTY_NAME_DB_URL = "db.url";
    private static final String PROPERTY_NAME_DB_USERNAME = "db.username";
    private static final String PROPERTY_NAME_JOOQ_SQL_DIALECT = "jooq.sql.dialect";

    @Autowired
    private Environment env;

    @Bean(destroyMethod = "close")
    public DataSource dataSource() throws SQLException, DatabaseUnitException {

        LOGGER.info("PROPERTY_NAME_DB_DRIVER: {}", env.getRequiredProperty(PROPERTY_NAME_DB_DRIVER));
        LOGGER.info("PROPERTY_NAME_DB_URL: {}", env.getRequiredProperty(PROPERTY_NAME_DB_URL));
        LOGGER.info("PROPERTY_NAME_DB_USERNAME: {}", env.getRequiredProperty(PROPERTY_NAME_DB_USERNAME));
        LOGGER.info("PROPERTY_NAME_DB_PASSWORD: {}", env.getRequiredProperty(PROPERTY_NAME_DB_PASSWORD));

        BoneCPDataSource dataSource = new BoneCPDataSource();

        dataSource.setDriverClass(env.getRequiredProperty(PROPERTY_NAME_DB_DRIVER));
        dataSource.setJdbcUrl(env.getRequiredProperty(PROPERTY_NAME_DB_URL));
        dataSource.setUsername(env.getRequiredProperty(PROPERTY_NAME_DB_USERNAME));
        dataSource.setPassword(env.getRequiredProperty(PROPERTY_NAME_DB_PASSWORD));

        return dataSource;
    }

    @Bean
    public DatabaseConfigBean dbUnitDatabaseConfig() throws DataTypeException {
        final DatabaseConfigBean configBean = new DatabaseConfigBean();
        configBean.setDatatypeFactory(dataTypeFactory());

        return configBean;
    }

    @Bean
    public PostgresqlDataTypeFactory dataTypeFactory() throws DataTypeException {
        DoccloudPostgresqlDataTypeFactory dataTypeFactory = new DoccloudPostgresqlDataTypeFactory();

        dataTypeFactory.createDataType(Types.OTHER, "jsonb");

        return dataTypeFactory;
    }
    @Bean
    public DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection() throws DatabaseUnitException, SQLException {
        DatabaseDataSourceConnectionFactoryBean connectionFactoryBean = new DatabaseDataSourceConnectionFactoryBean(dataSource());
        connectionFactoryBean.setDatabaseConfig(dbUnitDatabaseConfig());
        return connectionFactoryBean;
    }

    @Bean
    public LazyConnectionDataSourceProxy lazyConnectionDataSource() throws DatabaseUnitException, SQLException {
        return new LazyConnectionDataSourceProxy(dataSource());
    }

    @Bean
    public TransactionAwareDataSourceProxy transactionAwareDataSource() throws DatabaseUnitException, SQLException {
        return new TransactionAwareDataSourceProxy(lazyConnectionDataSource());
    }

    @Bean
    public DataSourceTransactionManager transactionManager() throws DatabaseUnitException, SQLException {
        return new DataSourceTransactionManager(lazyConnectionDataSource());
    }

    @Bean
    public DataSourceConnectionProvider connectionProvider() throws DatabaseUnitException, SQLException {
        return new DataSourceConnectionProvider(transactionAwareDataSource());
    }

    @Bean
    public JOOQToSpringExceptionTransformer jooqToSpringExceptionTransformer() {
        return new JOOQToSpringExceptionTransformer();
    }

    @Bean
    public DefaultConfiguration configuration() throws DatabaseUnitException, SQLException {
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
    public DefaultDSLContext dsl() throws DatabaseUnitException, SQLException {
        return new DefaultDSLContext(configuration());
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer() throws DatabaseUnitException, SQLException {

        LOGGER.info("PROPERTY_NAME_DB_SCHEMA_SCRIPT: {} ", env.getRequiredProperty(PROPERTY_NAME_DB_SCHEMA_SCRIPT));
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
