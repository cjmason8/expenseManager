package au.com.mason.expensemanager.task;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.robot.EmailTrawler;
import au.com.mason.expensemanager.service.DatabaseBackupService;

@Profile("production")
@Component
public class ScheduledTasks {

	private static Logger LOGGER = LogManager.getLogger(ScheduledTasks.class);

	@Autowired
	private EmailTrawler emailTrawler;

	@Autowired
	private DatabaseBackupService databaseBackupService;

	@Scheduled(cron = "0 0 */4 * * *")
	public void runEmailTrawler() {
		LOGGER.info("starting runEmailTrawler" + new Date());

		emailTrawler.check();

		LOGGER.info("ending runEmailTrawler" + new Date());
	}

	@Scheduled(cron = "${backup.daily.cron:0 30 23 * * *}", zone = "${backup.timezone:Australia/Melbourne}")
	public void runDailyDatabaseBackup() {
		LOGGER.info("starting runDailyDatabaseBackup {}", new Date());
		try {
			databaseBackupService.runDailyBackup();
		} catch (Exception e) {
			LOGGER.error("Daily database backup failed", e);
		}
		LOGGER.info("ending runDailyDatabaseBackup {}", new Date());
	}

	@Scheduled(cron = "${backup.weekly.cron:0 35 23 * * SUN}", zone = "${backup.timezone:Australia/Melbourne}")
	public void runWeeklyDatabaseBackup() {
		LOGGER.info("starting runWeeklyDatabaseBackup {}", new Date());
		try {
			databaseBackupService.runWeeklyBackup();
		} catch (Exception e) {
			LOGGER.error("Weekly database backup failed", e);
		}
		LOGGER.info("ending runWeeklyDatabaseBackup {}", new Date());
	}
}
