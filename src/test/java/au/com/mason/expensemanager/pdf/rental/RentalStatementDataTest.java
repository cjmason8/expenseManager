package au.com.mason.expensemanager.pdf.rental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class RentalStatementDataTest {

	@Test
	void isBalanced_returnsTrueWhenPaymentMatchesExpected() {
		RentalStatementData data = new RentalStatementData(
			new BigDecimal("2151.00"),
			new BigDecimal("141.96"),
			new BigDecimal("4.95"),
			LocalDate.of(2025, 11, 10),
			LocalDate.of(2025, 12, 10),
			new BigDecimal("2004.09"));

		assertTrue(data.isBalanced());
		assertEquals(new BigDecimal("2004.09"), data.expectedPaymentToOwner());
	}

	@Test
	void isBalanced_returnsFalseWhenPaymentDoesNotMatchExpected() {
		RentalStatementData data = new RentalStatementData(
			new BigDecimal("2151.00"),
			new BigDecimal("141.96"),
			new BigDecimal("4.95"),
			LocalDate.of(2025, 11, 10),
			LocalDate.of(2025, 12, 10),
			new BigDecimal("1.00"));

		assertFalse(data.isBalanced());
	}

}
