package au.com.mason.expensemanager.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public final class DockerPgDumpSupport {

	private DockerPgDumpSupport() {
	}

	public static Optional<String> findContainerId(String dockerPsOutput, String imageMatch) {
		if (StringUtils.isBlank(dockerPsOutput) || StringUtils.isBlank(imageMatch)) {
			return Optional.empty();
		}
		for (String line : dockerPsOutput.lines().toList()) {
			String trimmed = line.trim();
			if (trimmed.isEmpty()) {
				continue;
			}
			String[] parts = trimmed.split("\t", 2);
			if (parts.length == 2 && parts[1].contains(imageMatch)) {
				return Optional.of(parts[0].trim());
			}
		}
		return Optional.empty();
	}

	public static String resolveContainerId(String imageMatch) throws IOException, InterruptedException {
		Process process = new ProcessBuilder("docker", "ps", "--format", "{{.ID}}\t{{.Image}}").redirectErrorStream(true)
			.start();
		String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		int exitCode = process.waitFor();
		if (exitCode != 0) {
			throw new IllegalStateException("docker ps failed with exit code " + exitCode + ": " + output.trim());
		}
		return findContainerId(output, imageMatch)
			.orElseThrow(() -> new IllegalStateException("No running docker container matches image: " + imageMatch));
	}

	public static void runPgDump(String containerId, String username, String database, Path outputFile)
		throws IOException, InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder("docker", "exec", containerId, "pg_dump", "-U", username,
			database);
		processBuilder.redirectOutput(outputFile.toFile());
		processBuilder.redirectErrorStream(true);

		Process process = processBuilder.start();
		String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		int exitCode = process.waitFor();
		if (!output.isBlank()) {
			throw new IllegalStateException("pg_dump via docker exec failed: " + output.trim());
		}
		if (exitCode != 0) {
			throw new IllegalStateException("pg_dump via docker exec failed with exit code " + exitCode);
		}
		if (!Files.isRegularFile(outputFile) || Files.size(outputFile) == 0) {
			throw new IllegalStateException("pg_dump via docker exec produced no output at " + outputFile);
		}
	}

}
