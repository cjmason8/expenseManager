package au.com.mason.expensemanager.service;

import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

@Service
public class AwsSecretsService {

	private static final Logger LOGGER = LogManager.getLogger(AwsSecretsService.class);

	private SecretsManagerClient secretsManagerClient;
	private AwsCredentialsProvider credentialsProvider;
	private final Gson gson = new Gson();

	@Value("${aws.secrets.region:ap-southeast-2}")
	private String region;

	@Value("${aws.secrets.access-key:}")
	private String accessKey;

	@Value("${aws.secrets.secret-key:}")
	private String secretKey;

	@Value("${aws.secrets.session-token:}")
	private String sessionToken;

	@PostConstruct
	public void init() {
		var builder = SecretsManagerClient.builder().region(Region.of(region.trim()));

		if (StringUtils.isNotBlank(accessKey) && StringUtils.isNotBlank(secretKey)) {
			if (StringUtils.isNotBlank(sessionToken)) {
				credentialsProvider = StaticCredentialsProvider.create(AwsSessionCredentials.create(
						accessKey.trim(), secretKey.trim(), sessionToken.trim()));
				builder.credentialsProvider(credentialsProvider);
				LOGGER.info("Using AWS session credentials for Secrets Manager");
			}
			else {
				credentialsProvider = StaticCredentialsProvider.create(
						AwsBasicCredentials.create(accessKey.trim(), secretKey.trim()));
				builder.credentialsProvider(credentialsProvider);
				LOGGER.info("Using AWS basic credentials for Secrets Manager");
			}
		}
		else {
			credentialsProvider = DefaultCredentialsProvider.create();
			builder.credentialsProvider(credentialsProvider);
			LOGGER.info("Using default AWS credentials provider for Secrets Manager");
		}

		this.secretsManagerClient = builder.build();
		LOGGER.info("AWS Secrets Manager client initialized for region: {}", region);
	}

	@PreDestroy
	public void cleanup() {
		if (secretsManagerClient != null) {
			secretsManagerClient.close();
			LOGGER.info("AWS Secrets Manager client closed");
		}
	}

	/**
	 * Retrieves a secret value from AWS Secrets Manager.
	 *
	 * @param secretName the name or ARN of the secret
	 * @return the secret string value
	 * @throws SecretsManagerException if the secret cannot be retrieved
	 */
	public String getSecretString(String secretName) {
		try {
			GetSecretValueRequest request = GetSecretValueRequest.builder()
					.secretId(secretName)
					.build();

			GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
			String secret = response.secretString();

			LOGGER.info("Successfully retrieved secret: {}", secretName);
			return secret;
		}
		catch (SecretsManagerException e) {
			LOGGER.error("Failed to retrieve secret: {} - {}", secretName, e.getMessage());
			throw e;
		}
	}

	/**
	 * Retrieves a secret and parses it as JSON into a Map.
	 *
	 * @param secretName the name or ARN of the secret
	 * @return a map containing the secret's key-value pairs
	 * @throws SecretsManagerException if the secret cannot be retrieved
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> getSecretAsMap(String secretName) {
		String secretString = getSecretString(secretName);
		try {
			return gson.fromJson(secretString, Map.class);
		}
		catch (Exception e) {
			LOGGER.error("Failed to parse secret as JSON: {}", secretName, e);
			throw new IllegalStateException("Secret is not valid JSON: " + secretName, e);
		}
	}

	/**
	 * Retrieves a specific key from a JSON secret.
	 *
	 * @param secretName the name or ARN of the secret
	 * @param key the key to extract from the JSON secret
	 * @return the value associated with the key, or null if not found
	 * @throws SecretsManagerException if the secret cannot be retrieved
	 */
	public String getSecretValue(String secretName, String key) {
		Map<String, String> secretMap = getSecretAsMap(secretName);
		return secretMap.get(key);
	}

	public AwsCredentialsProvider getCredentialsProvider() {
		return credentialsProvider;
	}

}
