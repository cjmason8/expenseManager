package au.com.mason.expensemanager.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class S3Service {

	private static final Logger LOGGER = LogManager.getLogger(S3Service.class);

	private final S3Client s3Client;
	private final String bucket;

	public S3Service(AwsSecretsService awsSecretsService, @Value("${aws.s3.bucket:}") String bucket,
		@Value("${aws.s3.region:ap-southeast-2}") String region, @Value("${aws.s3.endpoint:}") String endpointOverride,
		@Value("${aws.s3.path-style-access:false}") boolean pathStyleAccess) {

		if (StringUtils.isBlank(bucket)) {
			throw new IllegalStateException("S3 bucket must be set via aws.s3.bucket / AWS_S3_BUCKET");
		}
		this.bucket = bucket.trim();

		var builder = S3Client.builder().region(Region.of(region.trim()))
			.credentialsProvider(awsSecretsService.getCredentialsProvider());

		if (StringUtils.isNotBlank(endpointOverride)) {
			builder.endpointOverride(URI.create(endpointOverride.trim()));
			builder.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(pathStyleAccess).build());
		}

		this.s3Client = builder.build();
		LOGGER.info("S3 client initialized for bucket {} in region {}", this.bucket, region);
	}

	public byte[] getObjectAsBytes(String key) {
		String k = normalizeKey(key);
		try {
			return s3Client
				.getObject(GetObjectRequest.builder().bucket(bucket).key(k).build(), ResponseTransformer.toBytes())
				.asByteArray();
		} catch (S3Exception e) {
			throw s3Failure("GetObject", k, e);
		}
	}

	public void deleteObject(String key) {
		String k = normalizeKey(key);
		s3Client.deleteObject(r -> r.bucket(bucket).key(k));
	}

	public boolean objectExists(String key) {
		String k = normalizeKey(key);
		try {
			s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(k).build());
			return true;
		} catch (S3Exception e) {
			if (e.statusCode() == 404) {
				return false;
			}
			throw s3Failure("HeadObject", k, e);
		}
	}

	public void moveObject(String sourceKey, String destinationKey) {
		String src = normalizeKey(sourceKey);
		String dst = normalizeKey(destinationKey);
		if (src.equals(dst)) {
			return;
		}
		ensureFolderMarkersForKey(dst);
		s3Client.copyObject(CopyObjectRequest.builder().sourceBucket(bucket).sourceKey(src).destinationBucket(bucket)
			.destinationKey(dst).build());
		s3Client.deleteObject(r -> r.bucket(bucket).key(src));
	}

	/**
	 * Lists and deletes every object whose key starts with {@code prefix + "/"} or
	 * equals {@code prefix}.
	 */
	public void deleteAllUnderPrefix(String prefix) {
		String p = normalizeKey(prefix);
		if (p.isEmpty()) {
			throw new IllegalArgumentException("refusing to delete entire bucket with empty prefix");
		}
		List<String> keys = listAllKeysUnderPrefix(p);
		if (keys.isEmpty()) {
			return;
		}
		batchDelete(keys);
	}

	/**
	 * Copies every object under {@code oldPrefix} to the same relative path under
	 * {@code newPrefix}, then deletes the old keys.
	 */
	public void renamePrefix(String oldPrefix, String newPrefix) {
		String op = normalizeKey(oldPrefix);
		String np = normalizeKey(newPrefix);
		if (op.equals(np)) {
			return;
		}
		List<String> keys = listKeysInTree(op);
		for (String key : keys) {
			String destKey = mapKeyUnderRename(key, op, np);
			ensureFolderMarkersForKey(destKey);
			s3Client.copyObject(CopyObjectRequest.builder().sourceBucket(bucket).sourceKey(key)
				.destinationBucket(bucket).destinationKey(destKey).build());
			s3Client.deleteObject(r -> r.bucket(bucket).key(key));
		}
	}

	private static String mapKeyUnderRename(String key, String oldPrefix, String newPrefix) {
		String op = oldPrefix;
		String np = newPrefix;
		if (key.equals(op)) {
			return np;
		}
		String opSlash = op + "/";
		if (key.equals(opSlash)) {
			return np + "/";
		}
		if (key.startsWith(opSlash)) {
			return np + "/" + key.substring(opSlash.length());
		}
		return key;
	}

	private List<String> listKeysInTree(String rootPrefix) {
		String p = normalizeKey(rootPrefix);
		LinkedHashSet<String> keys = new LinkedHashSet<>();
		String childPrefix = p + "/";
		String continuationToken = null;
		do {
			var reqBuilder = ListObjectsV2Request.builder().bucket(bucket).prefix(childPrefix);
			if (continuationToken != null) {
				reqBuilder.continuationToken(continuationToken);
			}
			var resp = s3Client.listObjectsV2(reqBuilder.build());
			for (S3Object obj : resp.contents()) {
				keys.add(obj.key());
			}
			continuationToken = resp.isTruncated() ? resp.nextContinuationToken() : null;
		} while (continuationToken != null);

		try {
			s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(childPrefix).build());
			keys.add(childPrefix);
		} catch (S3Exception e) {
			if (e.statusCode() != 404) {
				throw e;
			}
		}
		try {
			s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(p).build());
			keys.add(p);
		} catch (S3Exception e) {
			if (e.statusCode() != 404) {
				throw e;
			}
		}
		return new ArrayList<>(keys);
	}

	private List<String> listAllKeysUnderPrefix(String prefix) {
		return listKeysInTree(prefix);
	}

	private void batchDelete(List<String> keys) {
		final int batch = 1000;
		for (int i = 0; i < keys.size(); i += batch) {
			List<ObjectIdentifier> ids = new ArrayList<>();
			for (int j = i; j < Math.min(i + batch, keys.size()); j++) {
				ids.add(ObjectIdentifier.builder().key(keys.get(j)).build());
			}
			s3Client.deleteObjects(
				DeleteObjectsRequest.builder().bucket(bucket).delete(Delete.builder().objects(ids).build()).build());
		}
	}

	/**
	 * Ensures folder marker keys exist for every segment of {@code objectKey}
	 * (excluding the final segment).
	 */
	public void ensureFolderMarkersForKey(String objectKey) {
		String k = normalizeKey(objectKey);
		int lastSlash = k.lastIndexOf('/');
		if (lastSlash <= 0) {
			return;
		}
		ensureFolderMarkers(k.substring(0, lastSlash));
	}

	/**
	 * Ensures a trailing-slash folder marker exists for this prefix (and parents).
	 */
	public void ensureFolderPrefix(String folderKey) {
		String k = normalizeKey(folderKey);
		if (k.isEmpty()) {
			return;
		}
		ensureFolderMarkers(k);
		String marker = k + "/";
		putFolderMarkerIfAbsent(marker);
	}

	/**
	 * Uploads bytes to {@code {folderPrefix}/{objectId}}. Ensures each segment of
	 * {@code folderPrefix} exists as an S3 “folder” (zero-byte key ending in /)
	 * when missing.
	 */
	public void putObjectWithFolders(String folderPrefix, UUID objectId, byte[] data, String contentType) {
		String normalizedPrefix = normalizeKey(folderPrefix);
		try {
			ensureFolderMarkers(normalizedPrefix);

			String key = normalizedPrefix.isEmpty() ? objectId.toString() : normalizedPrefix + "/" + objectId;

			String resolvedContentType = StringUtils.isNotBlank(contentType) ? contentType : "application/octet-stream";

			s3Client.putObject(PutObjectRequest.builder().bucket(bucket).key(key).contentType(resolvedContentType)
				.contentLength((long) data.length).build(), RequestBody.fromBytes(data));
		} catch (S3Exception e) {
			throw s3Failure("PutObject(upload)",
				normalizedPrefix.isEmpty() ? objectId.toString() : normalizedPrefix + "/" + objectId, e);
		}
	}

	private void ensureFolderMarkers(String normalizedPrefix) {
		if (normalizedPrefix.isEmpty()) {
			return;
		}
		String[] parts = normalizedPrefix.split("/");
		StringBuilder acc = new StringBuilder();
		for (String part : parts) {
			if (part.isEmpty()) {
				continue;
			}
			if (acc.length() > 0) {
				acc.append('/');
			}
			acc.append(part);
			String folderKey = acc + "/";
			putFolderMarkerIfAbsent(folderKey);
		}
	}

	private void putFolderMarkerIfAbsent(String folderKey) {
		try {
			s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(folderKey).build());
		} catch (S3Exception e) {
			if (e.statusCode() == 404) {
				try {
					s3Client.putObject(PutObjectRequest.builder().bucket(bucket).key(folderKey).build(),
						RequestBody.fromBytes(new byte[0]));
				} catch (S3Exception putEx) {
					throw s3Failure("PutObject(folder marker)", folderKey, putEx);
				}
			} else {
				throw s3Failure("HeadObject(folder marker)", folderKey, e);
			}
		}
	}

	private IllegalStateException s3Failure(String operation, String objectKey, S3Exception e) {
		String detail = formatS3Error(e);
		LOGGER.error("S3 {} s3://{}/{} — {}", operation, bucket, objectKey, detail);
		String hint = "";
		if (e.statusCode() == 403) {
			hint = " Typical fix: IAM with s3:PutObject and s3:GetObject on arn:aws:s3:::" + bucket
				+ "/* (HeadObject requires GetObject permission on the key). Verify credentials in AWS Secrets Manager"
				+ " and match aws.s3.region to the bucket region.";
		}
		return new IllegalStateException(
			"S3 " + operation + " failed for s3://" + bucket + "/" + objectKey + ": " + detail + "." + hint, e);
	}

	private static String formatS3Error(S3Exception e) {
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP ").append(e.statusCode());
		if (e.awsErrorDetails() != null) {
			var d = e.awsErrorDetails();
			if (StringUtils.isNotBlank(d.errorCode())) {
				sb.append(" ").append(d.errorCode());
			}
			if (StringUtils.isNotBlank(d.errorMessage())) {
				sb.append(" — ").append(d.errorMessage());
			}
		} else if (StringUtils.isNotBlank(e.getMessage())) {
			sb.append(" — ").append(e.getMessage());
		}
		if (StringUtils.isNotBlank(e.requestId())) {
			sb.append(" (requestId=").append(e.requestId()).append(")");
		}
		return sb.toString();
	}

	private static String normalizeKey(String key) {
		if (key == null) {
			return "";
		}
		String p = key.trim().replace('\\', '/').replaceAll("/+", "/");
		while (p.startsWith("/")) {
			p = p.substring(1);
		}
		while (p.endsWith("/") && p.length() > 1) {
			p = p.substring(0, p.length() - 1);
		}
		return p;
	}

	@PreDestroy
	public void shutdown() {
		s3Client.close();
	}
}
