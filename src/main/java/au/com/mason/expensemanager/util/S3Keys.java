package au.com.mason.expensemanager.util;

/**
 * Normalizes and joins S3 object key segments (no leading slash, no duplicate slashes).
 */
public final class S3Keys {

	private S3Keys() {
	}

	public static String normalize(String key) {
		if (key == null) {
			return null;
		}
		String k = key.trim().replace('\\', '/').replaceAll("/+", "/");
		while (k.startsWith("/")) {
			k = k.substring(1);
		}
		while (k.endsWith("/") && k.length() > 1) {
			k = k.substring(0, k.length() - 1);
		}
		return k;
	}

	public static String join(String first, String second) {
		String a = normalize(first);
		String b = normalize(second);
		if (a == null || a.isEmpty()) {
			return b;
		}
		if (b == null || b.isEmpty()) {
			return a;
		}
		return a + "/" + b;
	}
}
