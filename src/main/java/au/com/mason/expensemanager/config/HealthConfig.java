package au.com.mason.expensemanager.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthConfig {

	@Bean
	@Autowired
	public HealthIndicator dbHealthIndicator(DataSource dataSource) {
		DataSourceHealthIndicator indicator = new DataSourceHealthIndicator(dataSource);
		indicator.setQuery("select count(*) from users");

		return indicator;
	}
}
