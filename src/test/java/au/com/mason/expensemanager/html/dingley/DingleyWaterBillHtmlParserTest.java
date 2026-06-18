package au.com.mason.expensemanager.html.dingley;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.com.mason.expensemanager.html.BillNoticeData;

class DingleyWaterBillHtmlParserTest {

	private DingleyWaterBillHtmlParser parser;

	@BeforeEach
	void setUp() {
		parser = new DingleyWaterBillHtmlParser();
	}

	@Test
	void parse_extractsDueDateAndAmount() {
		BillNoticeData bill = parser.parse(DingleyWaterBillFixtures.STANDARD_BILL);

		assertEquals(LocalDate.of(2026, 3, 15), bill.dueDate());
		assertEquals("123.45", bill.amount());
	}

	@Test
	void parse_stripsWbrTagsBeforeParsingDueDate() {
		BillNoticeData bill = parser.parse(DingleyWaterBillFixtures.BILL_WITH_WBR);

		assertEquals(LocalDate.of(2026, 3, 15), bill.dueDate());
		assertEquals("99.00", bill.amount());
	}

	@Test
	void parse_throwsWhenAmountMissing() {
		IllegalStateException error = assertThrows(IllegalStateException.class,
			() -> parser.parse("""
				<html><tr><td>Date due</td><td align="right">15 March 2026</td></tr></html>
				"""));

		assertTrue(error.getMessage().contains("amount"));
	}

	@Test
	void parse_throwsWhenDueDateMissing() {
		IllegalStateException error = assertThrows(IllegalStateException.class,
			() -> parser.parse("<html><p>$123.45</p></html>"));

		assertTrue(error.getMessage().contains("due date"));
	}

}
