package au.com.mason.expensemanager.html.telstra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.com.mason.expensemanager.html.BillNoticeData;

class TelstraBillHtmlParserTest {

	private TelstraBillHtmlParser parser;

	@BeforeEach
	void setUp() {
		parser = new TelstraBillHtmlParser();
	}

	@Test
	void parse_extractsDueDateAndAmount() {
		BillNoticeData bill = parser.parse(TelstraBillFixtures.STANDARD_BILL);

		assertEquals(LocalDate.of(2026, 6, 15), bill.dueDate());
		assertEquals("123.45", bill.amount());
	}

	@Test
	void parse_throwsWhenAmountMissing() {
		IllegalStateException error = assertThrows(IllegalStateException.class,
			() -> parser.parse("<html>Debit on15 Jun 2026</html>"));

		assertTrue(error.getMessage().contains("amount"));
	}

	@Test
	void parse_throwsWhenDueDateMissing() {
		IllegalStateException error = assertThrows(IllegalStateException.class,
			() -> parser.parse("<html>&#36;10.00&#160;</html>"));

		assertTrue(error.getMessage().contains("due date"));
	}

}
