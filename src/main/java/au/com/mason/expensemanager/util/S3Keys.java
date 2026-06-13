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

	/**
	 * Normalizes a UI / DB folder path ({@code /docs/...}). Preserves the leading {@code /docs} prefix.
	 */
	public static String toUiFolderPath(String path) {
		if (path == null || path.isBlank()) {
			return path;
		}
		String p = path.replace('\\', '/').trim();
		if (p.equals("docs")) {
			return "/docs";
		}
		if (p.startsWith("docs/")) {
			p = "/" + p;
		}
		if (p.equals("/docs")) {
			return p;
		}
		if (p.startsWith("/docs/")) {
			return trimTrailingSlash(p);
		}
		int docsIndex = p.indexOf("/docs/");
		if (docsIndex >= 0) {
			return trimTrailingSlash(p.substring(docsIndex));
		}
		if (p.endsWith("/docs")) {
			return "/docs";
		}
		return trimTrailingSlash(p);
	}

	/**
	 * Maps a UI / DB folder path to an S3 key prefix (path within the bucket, no leading slash).
	 */
	public static String toBucketPrefix(String path) {
		String uiPath = toUiFolderPath(path);
		if (uiPath == null || uiPath.isBlank()) {
			return uiPath;
		}
		if (uiPath.equals("/docs")) {
			return "";
		}
		if (uiPath.startsWith("/docs/")) {
			return normalize(uiPath.substring("/docs/".length()));
		}
		return normalize(uiPath);
	}

	private static String trimTrailingSlash(String path) {
		while (path.endsWith("/") && path.length() > 1) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}
}
