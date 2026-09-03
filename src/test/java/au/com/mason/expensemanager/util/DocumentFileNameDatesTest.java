package au.com.mason.expensemanager.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class DocumentFileNameDatesTest {

	@Test
	void extractDate_parsesDashedDate() {
		assertEquals(Optional.of(LocalDate.of(2026, 1, 1)),
			DocumentFileNameDates.extractDate("Payslip - 01-01-2026.pdf"));
	}

	@Test
	void extractDate_parsesCompactDate() {
		assertEquals(Optional.of(LocalDate.of(2026, 1, 1)),
			DocumentFileNameDates.extractDate("statement-01012026.pdf"));
	}

	@Test
	void extractDate_usesLatestDateWhenMultiplePresent() {
		assertEquals(Optional.of(LocalDate.of(2026, 3, 15)),
			DocumentFileNameDates.extractDate("report-01-01-2026-to-15-03-2026.pdf"));
	}

	@Test
	void extractDate_returnsEmptyWhenNoDate() {
		assertTrue(DocumentFileNameDates.extractDate("Annual Leave.pdf").isEmpty());
	}

}
