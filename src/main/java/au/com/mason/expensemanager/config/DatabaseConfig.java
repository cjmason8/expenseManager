package au.com.mason.expensemanager.config;

import java.util.Properties;

import javax.sql.DataSource;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import au.com.mason.expensemanager.service.EncryptionService;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
	
	@Autowired
	private EncryptionService encryptionService;

	/**
	 * DataSource definition for database connection. Settings are read from the
	 * application.properties file (using the env object).
	 */
	@Bean
	public DataSource dataSource() {
		DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
		dataSourceBuilder.driverClassName(System.getenv().get("DB_DRIVER"));
		dataSourceBuilder.url(System.getenv().get("DB_URL"));
		dataSourceBuilder.username(System.getenv().get("DB_USER"));
		dataSourceBuilder.password(encryptionService.decrypt(System.getenv().get("DB_PASS")));

		return dataSourceBuilder.build();
	}

}
