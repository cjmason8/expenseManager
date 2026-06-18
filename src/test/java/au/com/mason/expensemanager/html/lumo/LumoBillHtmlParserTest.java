package au.com.mason.expensemanager.html.lumo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.com.mason.expensemanager.html.BillNoticeData;

class LumoBillHtmlParserTest {

	private LumoBillHtmlParser parser;

	@BeforeEach
	void setUp() {
		parser = new LumoBillHtmlParser();
	}

	@Test
	void parse_extractsDueDateAndAmount() {
		BillNoticeData bill = parser.parse(LumoBillFixtures.STANDARD_BILL);

		assertEquals(LocalDate.of(2026, 6, 15), bill.dueDate());
		assertEquals("$450.00", bill.amount());
	}

	@Test
	void parse_throwsWhenAmountMissing() {
		IllegalStateException error = assertThrows(IllegalStateException.class, () -> parser.parse("""
			<html><p>DUE DATE</p><td style="color:white">15 Jun 26</td></html>
			"""));

		assertTrue(error.getMessage().contains("amount"));
	}

	@Test
	void parse_throwsWhenDueDateMissing() {
		IllegalStateException error = assertThrows(IllegalStateException.class,
			() -> parser.parse("<html><p>TOTAL AMOUNT DUE</p><td>$450.00</td></html>"));

		assertTrue(error.getMessage().contains("due date"));
	}

}
