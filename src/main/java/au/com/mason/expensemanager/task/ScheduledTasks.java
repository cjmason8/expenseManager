package au.com.mason.expensemanager.task;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.robot.EmailTrawler;

@Profile("production")
@Component
public class ScheduledTasks {
	
	private static Logger LOGGER = LogManager.getLogger(ScheduledTasks.class);
	
    @Autowired
	private EmailTrawler emailTrawler;

    @Scheduled(cron = "0 0 */4 * * *")
    public void runEmailTrawler() {
    	LOGGER.info("starting runEmailTrawler" + new Date());
    	
    	emailTrawler.check();
    	
    	LOGGER.info("ending runEmailTrawler" + new Date());
    }
}
