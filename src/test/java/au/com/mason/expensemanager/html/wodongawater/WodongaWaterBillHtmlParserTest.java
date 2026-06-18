package au.com.mason.expensemanager.html.wodongawater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.com.mason.expensemanager.html.BillNoticeData;

class WodongaWaterBillHtmlParserTest {

	private WodongaWaterBillHtmlParser parser;

	@BeforeEach
	void setUp() {
		parser = new WodongaWaterBillHtmlParser();
	}

	@Test
	void parse_extractsDueDateAndAmount() {
		BillNoticeData bill = parser.parse(WodongaWaterBillFixtures.STANDARD_BILL);

		assertEquals(LocalDate.of(2026, 3, 15), bill.dueDate());
		assertEquals("123.45", bill.amount());
	}

	@Test
	void parse_throwsWhenAmountMissing() {
		IllegalStateException error = assertThrows(IllegalStateException.class,
			() -> parser.parse("<html><span>Due Date zwnj 15 March 2026</span></html>"));

		assertTrue(error.getMessage().contains("amount"));
	}

	@Test
	void parse_throwsWhenDueDateMissing() {
		IllegalStateException error = assertThrows(IllegalStateException.class,
			() -> parser.parse("<html><span>Amount Due zwnj $123.45</span></html>"));

		assertTrue(error.getMessage().contains("due date"));
	}

}
