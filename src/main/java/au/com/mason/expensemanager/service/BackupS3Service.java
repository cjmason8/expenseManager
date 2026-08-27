package au.com.mason.expensemanager.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
@Profile("production")
public class BackupS3Service {

	private static final Logger LOGGER = LogManager.getLogger(BackupS3Service.class);

	private final S3Client s3Client;
	private final String bucket;

	public BackupS3Service(AwsSecretsService awsSecretsService,
		@Value("${aws.s3.backup-bucket:expense-manager-backups}") String bucket,
		@Value("${aws.s3.region:ap-southeast-2}") String region, @Value("${aws.s3.endpoint:}") String endpointOverride,
		@Value("${aws.s3.path-style-access:false}") boolean pathStyleAccess) {

		if (StringUtils.isBlank(bucket)) {
			throw new IllegalStateException(
				"Backup S3 bucket must be set via aws.s3.backup-bucket / AWS_S3_BACKUP_BUCKET");
		}
		this.bucket = bucket.trim();

		var builder = S3Client.builder().region(Region.of(region.trim()))
			.credentialsProvider(awsSecretsService.getCredentialsProvider());

		if (StringUtils.isNotBlank(endpointOverride)) {
			builder.endpointOverride(URI.create(endpointOverride.trim()));
			builder.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(pathStyleAccess).build());
		}

		this.s3Client = builder.build();
		LOGGER.info("Backup S3 client initialized for bucket {} in region {}", this.bucket, region);
	}

	public void putObject(String key, byte[] data, String contentType) {
		String normalizedKey = normalizeKey(key);
		try {
			s3Client.putObject(PutObjectRequest.builder().bucket(bucket).key(normalizedKey).contentType(contentType)
				.contentLength((long) data.length).build(), RequestBody.fromBytes(data));
		} catch (S3Exception e) {
			throw new IllegalStateException("S3 PutObject failed for s3://" + bucket + "/" + normalizedKey, e);
		}
	}

	public byte[] getObjectAsBytes(String key) {
		String normalizedKey = normalizeKey(key);
		try {
			return s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(normalizedKey).build(),
				ResponseTransformer.toBytes()).asByteArray();
		} catch (S3Exception e) {
			throw new IllegalStateException("S3 GetObject failed for s3://" + bucket + "/" + normalizedKey, e);
		}
	}

	public void deleteObject(String key) {
		s3Client.deleteObject(r -> r.bucket(bucket).key(normalizeKey(key)));
	}

	public List<String> listObjectKeys(String prefix) {
		String normalizedPrefix = normalizeKey(prefix);
		List<String> keys = new ArrayList<>();
		String continuationToken = null;
		do {
			var reqBuilder = ListObjectsV2Request.builder().bucket(bucket).prefix(normalizedPrefix);
			if (continuationToken != null) {
				reqBuilder.continuationToken(continuationToken);
			}
			var resp = s3Client.listObjectsV2(reqBuilder.build());
			for (S3Object obj : resp.contents()) {
				String key = obj.key();
				if (!key.endsWith("/")) {
					keys.add(key);
				}
			}
			continuationToken = resp.isTruncated() ? resp.nextContinuationToken() : null;
		} while (continuationToken != null);
		keys.sort(Comparator.naturalOrder());
		return keys;
	}

	public boolean objectExists(String key) {
		try {
			s3Client.headObject(r -> r.bucket(bucket).key(normalizeKey(key)));
			return true;
		} catch (S3Exception e) {
			if (e.statusCode() == 404) {
				return false;
			}
			throw e;
		}
	}

	public String bucket() {
		return bucket;
	}

	private static String normalizeKey(String key) {
		if (key == null) {
			return "";
		}
		String p = key.trim().replace('\\', '/').replaceAll("/+", "/");
		while (p.startsWith("/")) {
			p = p.substring(1);
		}
		return p;
	}

	@PreDestroy
	public void shutdown() {
		s3Client.close();
	}

}
