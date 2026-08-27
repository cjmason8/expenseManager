package au.com.mason.expensemanager.util;

import org.apache.commons.lang3.StringUtils;

public record JdbcConnectionInfo(String host, String port, String database) {

	public static JdbcConnectionInfo fromJdbcUrl(String jdbcUrl) {
		if (StringUtils.isBlank(jdbcUrl)) {
			throw new IllegalStateException("DB_URL is not set");
		}
		String withoutPrefix = jdbcUrl.trim().replaceFirst("^jdbc:postgresql://", "");
		int slash = withoutPrefix.indexOf('/');
		if (slash < 0) {
			throw new IllegalStateException("DB_URL missing database name: " + jdbcUrl);
		}
		String hostPort = withoutPrefix.substring(0, slash);
		String databasePart = withoutPrefix.substring(slash + 1);
		int question = databasePart.indexOf('?');
		String database = question >= 0 ? databasePart.substring(0, question) : databasePart;
		if (database.isBlank()) {
			throw new IllegalStateException("DB_URL missing database name: " + jdbcUrl);
		}

		int colon = hostPort.indexOf(':');
		if (colon < 0) {
			return new JdbcConnectionInfo(hostPort, "5432", database);
		}
		return new JdbcConnectionInfo(hostPort.substring(0, colon), hostPort.substring(colon + 1), database);
	}

}
