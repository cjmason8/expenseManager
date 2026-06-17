package au.com.mason.expensemanager.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class LegacyDocumentIdMappingTest {

	@Test
	void legacyIdMatchesMigrationSqlShape() {
		// echo "select ... md5('expensemanager.documents.pk:' || 1::text) ..." in
		// PostgreSQL yields this UUID layout
		assertEquals(UUID.fromString("1a6eddb3-a53f-41e4-ab7a-5563bc350061"),
			LegacyDocumentIdMapping.uuidFromLegacyLong(1L));
	}
}
