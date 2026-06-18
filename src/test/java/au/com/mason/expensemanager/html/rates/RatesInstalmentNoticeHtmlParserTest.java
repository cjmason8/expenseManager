package au.com.mason.expensemanager.html.rates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.com.mason.expensemanager.html.BillNoticeData;

class RatesInstalmentNoticeHtmlParserTest {

	private RatesInstalmentNoticeHtmlParser parser;

	@BeforeEach
	void setUp() {
		parser = new RatesInstalmentNoticeHtmlParser();
	}

	@Test
	void parse_extractsDueDateAndAmount() {
		BillNoticeData notice = parser.parse(RatesInstalmentNoticeFixtures.STANDARD_NOTICE);

		assertEquals(LocalDate.of(2026, 3, 15), notice.dueDate());
		assertEquals("$500.00", notice.amount());
	}

	@Test
	void parse_throwsWhenDueDateMissing() {
		IllegalStateException error = assertThrows(IllegalStateException.class,
			() -> parser.parse("<html><td>Amount Due</td><td class=\"value\">$100.00</td></html>"));

		assertTrue(error.getMessage().contains("due date"));
	}

	@Test
	void parse_throwsWhenAmountMissing() {
		IllegalStateException error = assertThrows(IllegalStateException.class,
			() -> parser.parse("<html><td>Due Date</td><td class=\"value\">15 March 2026</td></html>"));

		assertTrue(error.getMessage().contains("amount due"));
	}

}
