package au.com.mason.expensemanager.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class MappingConvertersTest {

	@Test
	void stringToBigDecimal_parsesPlainNumber() {
		assertEquals(new BigDecimal("1234.56"), MappingConverters.stringToBigDecimal("1234.56"));
	}

	@Test
	void stringToBigDecimal_stripsCommasAndDollarSign() {
		assertEquals(new BigDecimal("1234.56"), MappingConverters.stringToBigDecimal("$1,234.56"));
	}

	@Test
	void stringToBigDecimal_returnsNullForBlank() {
		assertNull(MappingConverters.stringToBigDecimal(""));
		assertNull(MappingConverters.stringToBigDecimal("  "));
	}

}
