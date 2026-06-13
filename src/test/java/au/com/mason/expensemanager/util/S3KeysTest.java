package au.com.mason.expensemanager.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class S3KeysTest {

	@Test
	void toUiFolderPath_preservesDocsPrefix() {
		assertEquals("/docs/expenseManager/filofax", S3Keys.toUiFolderPath("/docs/expenseManager/filofax"));
		assertEquals("/docs/expenseManager/filofax", S3Keys.toUiFolderPath("docs/expenseManager/filofax"));
		assertEquals("/docs", S3Keys.toUiFolderPath("/docs"));
	}

	@Test
	void toBucketPrefix_stripsDocsPrefix() {
		assertEquals("expenseManager/filofax", S3Keys.toBucketPrefix("/docs/expenseManager/filofax"));
		assertEquals("expenseManager/expenses", S3Keys.toBucketPrefix("docs/expenseManager/expenses"));
		assertEquals("", S3Keys.toBucketPrefix("/docs"));
	}

}
