package au.com.mason.expensemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories("au.com.mason.expensemanager.repository")
public class ExpenseManagerApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(ExpenseManagerApplication.class, args);
	}
}
