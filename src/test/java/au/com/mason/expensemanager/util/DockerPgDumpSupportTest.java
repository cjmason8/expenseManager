package au.com.mason.expensemanager.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class DockerPgDumpSupportTest {

	@Test
	void findContainerId_matchesPostgresImage() {
		String dockerPs = """
			abc123def456	postgres:13.4-alpine
			789ghi012jkl	expensemanager:latest
			""";

		Optional<String> containerId = DockerPgDumpSupport.findContainerId(dockerPs, "postgres:13.4-alpine");

		assertTrue(containerId.isPresent());
		assertEquals("abc123def456", containerId.get());
	}

	@Test
	void findContainerId_returnsEmptyWhenNoMatch() {
		String dockerPs = "789ghi012jkl\texpensemanager:latest";

		assertTrue(DockerPgDumpSupport.findContainerId(dockerPs, "postgres:13.4-alpine").isEmpty());
	}

}
