package au.com.mason.expensemanager.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import au.com.mason.expensemanager.util.DockerPgDumpSupport;
import au.com.mason.expensemanager.util.JdbcConnectionInfo;

@Service
@Profile("production")
public class DatabaseBackupService {

	private static final Logger LOGGER = LogManager.getLogger(DatabaseBackupService.class);

	static final String DAILY_PREFIX = "daily/";
	static final String WEEKLY_PREFIX = "weekly/";
	static final DateTimeFormatter DATE_STAMP = DateTimeFormatter.ISO_LOCAL_DATE;
	static final int WEEKLY_RETENTION = 2;
	static final String PG_DUMP_MODE_DOCKER = "docker";
	static final String PG_DUMP_MODE_DIRECT = "direct";

	@Autowired
	private BackupS3Service backupS3Service;

	@Autowired
	private AwsSecretsService awsSecretsService;

	@Value("${database.secret.name:local-database-credentials}")
	private String databaseSecretName;

	@Value("${backup.timezone:Australia/Melbourne}")
	private String backupTimezone;

	@Value("${backup.pg-dump.mode:direct}")
	private String pgDumpMode;

	@Value("${backup.docker.image-match:postgres:13.4-alpine}")
	private String dockerImageMatch;

	@Value("${backup.docker.username:postgres}")
	private String dockerPgUsername;

	@Value("${backup.docker.database:expensemanager}")
	private String dockerPgDatabase;

	public void runDailyBackup() throws Exception {
		LocalDate backupDate = LocalDate.now(zone());
		Path dumpFile = Files.createTempFile("expensemanager-backup-", ".sql");
		try {
			runPgDump(dumpFile);
			byte[] dumpBytes = Files.readAllBytes(dumpFile);
			String key = dailyKey(backupDate);
			backupS3Service.putObject(key, dumpBytes, "application/sql");
			LOGGER.info("Uploaded daily database backup to s3://{}/{} ({} bytes)", backupS3Service.bucket(), key,
				dumpBytes.length);
		} finally {
			Files.deleteIfExists(dumpFile);
		}
	}

	public void runWeeklyBackup() throws Exception {
		LocalDate sunday = LocalDate.now(zone());
		if (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
			LOGGER.warn("Weekly backup skipped because today is {} not Sunday", sunday);
			return;
		}

		LocalDate monday = sunday.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		List<String> includedKeys = new ArrayList<>();
		ByteArrayOutputStream zipBuffer = new ByteArrayOutputStream();
		try (ZipOutputStream zipOut = new ZipOutputStream(zipBuffer)) {
			for (LocalDate date = monday; !date.isAfter(sunday); date = date.plusDays(1)) {
				String key = dailyKey(date);
				if (!backupS3Service.objectExists(key)) {
					LOGGER.warn("Daily backup missing for weekly archive: {}", key);
					continue;
				}
				byte[] dumpBytes = backupS3Service.getObjectAsBytes(key);
				String entryName = Path.of(key).getFileName().toString();
				zipOut.putNextEntry(new ZipEntry(entryName));
				zipOut.write(dumpBytes);
				zipOut.closeEntry();
				includedKeys.add(key);
			}
		}

		if (includedKeys.isEmpty()) {
			throw new IllegalStateException("Weekly backup found no daily dumps for week starting " + monday);
		}

		String weeklyKey = weeklyKey(sunday);
		backupS3Service.putObject(weeklyKey, zipBuffer.toByteArray(), "application/zip");
		LOGGER.info("Uploaded weekly database backup to s3://{}/{} ({} daily dumps)", backupS3Service.bucket(),
			weeklyKey, includedKeys.size());

		deleteDailyBackups(includedKeys);
		trimWeeklyBackups();
	}

	static String dailyKey(LocalDate date) {
		return DAILY_PREFIX + "expensemanager-" + DATE_STAMP.format(date) + ".sql";
	}

	static String weeklyKey(LocalDate sunday) {
		return WEEKLY_PREFIX + "expensemanager-week-" + DATE_STAMP.format(sunday) + ".zip";
	}

	static List<String> selectWeeklyKeysToDelete(List<String> weeklyKeys, int retention) {
		List<String> sorted = weeklyKeys.stream().sorted(Comparator.reverseOrder()).toList();
		if (sorted.size() <= retention) {
			return List.of();
		}
		return sorted.subList(retention, sorted.size());
	}

	private void trimWeeklyBackups() {
		List<String> weeklyKeys = backupS3Service.listObjectKeys(WEEKLY_PREFIX).stream()
			.filter(key -> key.endsWith(".zip")).toList();
		for (String key : selectWeeklyKeysToDelete(weeklyKeys, WEEKLY_RETENTION)) {
			backupS3Service.deleteObject(key);
			LOGGER.info("Deleted old weekly backup s3://{}/{}", backupS3Service.bucket(), key);
		}
	}

	private void deleteDailyBackups(List<String> dailyKeys) {
		for (String key : dailyKeys) {
			backupS3Service.deleteObject(key);
			LOGGER.info("Deleted daily backup archived in weekly zip s3://{}/{}", backupS3Service.bucket(), key);
		}
	}

	private void runPgDump(Path outputFile) throws Exception {
		if (PG_DUMP_MODE_DIRECT.equalsIgnoreCase(pgDumpMode)) {
			runDirectPgDump(outputFile);
			return;
		}
		runDockerPgDump(outputFile);
	}

	private void runDockerPgDump(Path outputFile) throws Exception {
		String containerId = DockerPgDumpSupport.resolveContainerId(dockerImageMatch);
		LOGGER.info("Running pg_dump via docker exec in container {} ({})", containerId, dockerImageMatch);
		DockerPgDumpSupport.runPgDump(containerId, dockerPgUsername, dockerPgDatabase, outputFile);
	}

	private void runDirectPgDump(Path outputFile) throws Exception {
		String jdbcUrl = System.getenv("DB_URL");
		JdbcConnectionInfo connection = JdbcConnectionInfo.fromJdbcUrl(jdbcUrl);
		String username = awsSecretsService.getSecretValue(databaseSecretName, "USER_NAME");
		String password = awsSecretsService.getSecretValue(databaseSecretName, "PASSWORD");

		ProcessBuilder processBuilder = new ProcessBuilder("pg_dump", "-h", connection.host(), "-p", connection.port(),
			"-U", username, "-d", connection.database(), "-f", outputFile.toString());
		processBuilder.environment().put("PGPASSWORD", password);
		processBuilder.redirectErrorStream(true);

		LOGGER.info("Running pg_dump for database {} on {}:{}", connection.database(), connection.host(),
			connection.port());
		Process process = processBuilder.start();
		String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		int exitCode = process.waitFor();
		if (!output.isBlank()) {
			LOGGER.info("pg_dump output: {}", output.trim());
		}
		if (exitCode != 0) {
			throw new IllegalStateException("pg_dump failed with exit code " + exitCode);
		}
		if (!Files.isRegularFile(outputFile) || Files.size(outputFile) == 0) {
			throw new IllegalStateException("pg_dump produced no output at " + outputFile);
		}
	}

	private ZoneId zone() {
		return ZoneId.of(backupTimezone);
	}

}
