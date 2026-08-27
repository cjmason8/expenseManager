package au.com.mason.expensemanager.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JdbcConnectionInfoTest {

	@Test
	void fromJdbcUrl_parsesHostPortAndDatabase() {
		JdbcConnectionInfo info = JdbcConnectionInfo
			.fromJdbcUrl("jdbc:postgresql://db.example.com:5430/expensemanager?ssl=true");

		assertEquals("db.example.com", info.host());
		assertEquals("5430", info.port());
		assertEquals("expensemanager", info.database());
	}

	@Test
	void fromJdbcUrl_defaultsPortWhenMissing() {
		JdbcConnectionInfo info = JdbcConnectionInfo.fromJdbcUrl("jdbc:postgresql://localhost/expensemanager");

		assertEquals("localhost", info.host());
		assertEquals("5432", info.port());
		assertEquals("expensemanager", info.database());
	}

	@Test
	void fromJdbcUrl_rejectsMissingDatabase() {
		assertThrows(IllegalStateException.class,
			() -> JdbcConnectionInfo.fromJdbcUrl("jdbc:postgresql://localhost:5432/"));
	}

}
