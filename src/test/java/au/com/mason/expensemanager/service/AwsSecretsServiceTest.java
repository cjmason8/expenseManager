package au.com.mason.expensemanager.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AwsSecretsServiceTest {

	private AwsSecretsService awsSecretsService;

	@BeforeEach
	void setUp() {
		awsSecretsService = new AwsSecretsService();
		ReflectionTestUtils.setField(awsSecretsService, "enabled", false);
		ReflectionTestUtils.setField(awsSecretsService, "region", "ap-southeast-2");
	}

	@Test
	void testIsEnabled_WhenDisabled_ReturnsFalse() {
		assertFalse(awsSecretsService.isEnabled());
	}

	@Test
	void testIsEnabled_WhenEnabled_ReturnsTrue() {
		ReflectionTestUtils.setField(awsSecretsService, "enabled", true);
		assertTrue(awsSecretsService.isEnabled());
	}

	@Test
	void testGetSecretString_WhenDisabled_ThrowsException() {
		Exception exception = assertThrows(IllegalStateException.class, () -> {
			awsSecretsService.getSecretString("test-secret");
		});
		assertEquals("AWS Secrets Manager is disabled", exception.getMessage());
	}

	@Test
	void testGetSecretAsMap_WhenDisabled_ThrowsException() {
		Exception exception = assertThrows(IllegalStateException.class, () -> {
			awsSecretsService.getSecretAsMap("test-secret");
		});
		assertEquals("AWS Secrets Manager is disabled", exception.getMessage());
	}

	@Test
	void testGetSecretValue_WhenDisabled_ThrowsException() {
		Exception exception = assertThrows(IllegalStateException.class, () -> {
			awsSecretsService.getSecretValue("test-secret", "key");
		});
		assertEquals("AWS Secrets Manager is disabled", exception.getMessage());
	}
}
