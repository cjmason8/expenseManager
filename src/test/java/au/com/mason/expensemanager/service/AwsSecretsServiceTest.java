package au.com.mason.expensemanager.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AwsSecretsServiceTest {

	private AwsSecretsService awsSecretsService;

	@BeforeEach
	void setUp() {
		awsSecretsService = new AwsSecretsService();
		ReflectionTestUtils.setField(awsSecretsService, "region", "ap-southeast-2");
	}

	@Test
	void testGetSecretString_BeforeInit_ThrowsNullPointerException() {
		assertThrows(NullPointerException.class, () -> awsSecretsService.getSecretString("test-secret"));
	}

	@Test
	void testGetSecretAsMap_BeforeInit_ThrowsNullPointerException() {
		assertThrows(NullPointerException.class, () -> awsSecretsService.getSecretAsMap("test-secret"));
	}

	@Test
	void testGetSecretValue_BeforeInit_ThrowsNullPointerException() {
		assertThrows(NullPointerException.class, () -> awsSecretsService.getSecretValue("test-secret", "key"));
	}

}
