package au.com.mason.expensemanager.html.vicroads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.com.mason.expensemanager.html.BillNoticeData;

class VicRoadsBillHtmlParserTest {

	private VicRoadsBillHtmlParser parser;

	@BeforeEach
	void setUp() {
		parser = new VicRoadsBillHtmlParser();
	}

	@Test
	void parse_extractsDueDateAndAmount() {
		BillNoticeData bill = parser.parse(VicRoadsBillFixtures.STANDARD_BILL);

		assertEquals(LocalDate.of(2026, 6, 20), bill.dueDate());
		assertEquals("180.00", bill.amount());
	}

	@Test
	void parse_throwsWhenAmountMissing() {
		IllegalStateException error = assertThrows(IllegalStateException.class,
			() -> parser.parse("""
				<html><p>Due date</p><span class="em_black_link">20 June 2026</span></html>
				"""));

		assertTrue(error.getMessage().contains("amount"));
	}

	@Test
	void parse_throwsWhenDueDateMissing() {
		IllegalStateException error = assertThrows(IllegalStateException.class,
			() -> parser.parse("<html><p>$180.00</p></html>"));

		assertTrue(error.getMessage().contains("due date"));
	}

}
