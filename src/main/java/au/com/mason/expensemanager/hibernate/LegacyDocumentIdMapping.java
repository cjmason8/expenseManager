package au.com.mason.expensemanager.hibernate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Matches {@code migration.sql}: deterministic UUID from legacy
 * {@code documents.id} (bigint), using
 * {@code md5('expensemanager.documents.pk:' || old_id::text)} then
 * version-4-shaped layout.
 */
public final class LegacyDocumentIdMapping {

	private LegacyDocumentIdMapping() {
	}

	public static UUID uuidFromLegacyLong(long oldId) {
		byte[] digest = md5Utf8("expensemanager.documents.pk:" + oldId);
		String h = HexFormat.of().formatHex(digest);
		String s = h.substring(0, 8) + "-" + h.substring(8, 12) + "-4" + h.substring(12, 15) + "-a"
			+ h.substring(16, 19) + "-" + h.substring(20, 32);
		return UUID.fromString(s);
	}

	private static byte[] md5Utf8(String text) {
		try {
			return MessageDigest.getInstance("MD5").digest(text.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("MD5 not available", e);
		}
	}
}
