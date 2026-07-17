package au.com.mason.expensemanager.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import au.com.mason.expensemanager.service.AwsSecretsService;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

	private static final Logger LOGGER = LogManager.getLogger(DatabaseConfig.class);
	private static final String DEFAULT_DIALECT = "org.hibernate.dialect.PostgreSQLDialect";
	private static final String LEGACY_DIALECT = "au.com.mason.expensemanager.dao.MyPostgreSQL94Dialect";

	@Autowired
	private AwsSecretsService awsSecretsService;

	@Value("${database.secret.name:local-database-credentials}")
	private String databaseSecretName;

	/**
	 * DataSource definition for database connection. Settings are read from the
	 * application.properties file (using the env object).
	 */
	@Bean
	public DataSource dataSource() {
		String dbUrl = System.getenv("DB_URL");
		LOGGER.info("Configuring DataSource with DB_URL host={}", extractJdbcHost(dbUrl));

		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(System.getenv().get("DB_DRIVER"));
		dataSource.setUrl(dbUrl);
		dataSource.setUsername(awsSecretsService.getSecretValue(databaseSecretName, "USER_NAME"));
		dataSource.setPassword(awsSecretsService.getSecretValue(databaseSecretName, "PASSWORD"));

		return dataSource;
	}

	/**
	 * Declare the JPA entity manager factory.
	 */
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();

		entityManagerFactory.setDataSource(dataSource);

		// Classpath scanning of @Component, @Service, etc annotated class
		entityManagerFactory.setPackagesToScan("au.com.mason.expensemanager");

		// Vendor adapter
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		entityManagerFactory.setJpaVendorAdapter(vendorAdapter);

		// Hibernate properties
		Properties additionalProperties = new Properties();
		additionalProperties.put("hibernate.dialect", resolveHibernateDialect());
		additionalProperties.put("hibernate.hbm2ddl.auto", System.getenv().get("HIBERNATE_HBM2DDL_AUTO"));
		// additionalProperties.put("hibernate.temp.use_jdbc_metadata_defaults", false);
		entityManagerFactory.setJpaProperties(additionalProperties);

		return entityManagerFactory;
	}

	/**
	 * Declare the transaction manager.
	 */
	@Bean
	public JpaTransactionManager transactionManager() {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
		return transactionManager;
	}

	/**
	 * PersistenceExceptionTranslationPostProcessor is a bean post processor which
	 * adds an advisor to any bean annotated with Repository so that any
	 * platform-specific exceptions are caught and then rethrown as one Spring's
	 * unchecked data access exceptions (i.e. a subclass of DataAccessException).
	 */
	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}

	private String resolveHibernateDialect() {
		String dialect = System.getenv("HIBERNATE_DIALECT");
		if (dialect == null || dialect.isBlank() || LEGACY_DIALECT.equals(dialect)) {
			return DEFAULT_DIALECT;
		}
		return dialect;
	}

	private String extractJdbcHost(String dbUrl) {
		if (dbUrl == null || dbUrl.isBlank()) {
			return "<unset>";
		}
		try {
			String withoutPrefix = dbUrl.replaceFirst("^jdbc:postgresql://", "");
			int slash = withoutPrefix.indexOf('/');
			String hostPort = slash >= 0 ? withoutPrefix.substring(0, slash) : withoutPrefix;
			int question = hostPort.indexOf('?');
			return question >= 0 ? hostPort.substring(0, question) : hostPort;
		} catch (Exception e) {
			return "<unparseable>";
		}
	}

	// Private fields

	@Autowired
	private DataSource dataSource;

	@Autowired
	private LocalContainerEntityManagerFactoryBean entityManagerFactory;

}
