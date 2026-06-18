package au.com.mason.expensemanager.html.racv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.com.mason.expensemanager.html.BillNoticeData;

class RacvBillHtmlParserTest {

	private RacvBillHtmlParser parser;

	@BeforeEach
	void setUp() {
		parser = new RacvBillHtmlParser();
	}

	@Test
	void parse_extractsDueDateAndAmount() {
		BillNoticeData bill = parser.parse(RacvBillFixtures.STANDARD_BILL);

		assertEquals(LocalDate.of(2026, 3, 15), bill.dueDate());
		assertEquals("250.50", bill.amount());
	}

	@Test
	void parse_throwsWhenAmountMissing() {
		IllegalStateException error = assertThrows(IllegalStateException.class,
			() -> parser.parse("<html><p>Due 15/03/2026</p></html>"));

		assertTrue(error.getMessage().contains("amount"));
	}

	@Test
	void parse_throwsWhenDueDateMissing() {
		IllegalStateException error = assertThrows(IllegalStateException.class,
			() -> parser.parse("<html><p>$250.50</p></html>"));

		assertTrue(error.getMessage().contains("due date"));
	}

}
