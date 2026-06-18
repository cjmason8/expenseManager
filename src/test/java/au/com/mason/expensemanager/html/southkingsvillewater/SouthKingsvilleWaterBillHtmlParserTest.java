package au.com.mason.expensemanager.html.southkingsvillewater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.com.mason.expensemanager.html.BillDownloadNoticeData;

class SouthKingsvilleWaterBillHtmlParserTest {

	private SouthKingsvilleWaterBillHtmlParser parser;

	@BeforeEach
	void setUp() {
		parser = new SouthKingsvilleWaterBillHtmlParser();
	}

	@Test
	void parse_extractsDueDateAmountAndDownloadUrl() {
		BillDownloadNoticeData bill = parser.parse(SouthKingsvilleWaterBillFixtures.STANDARD_BILL);

		assertEquals(LocalDate.of(2026, 1, 15), bill.dueDate());
		assertEquals("123.45", bill.amount());
		assertEquals("https://example.com/bill.pdf", bill.downloadUrl());
	}

	@Test
	void parse_throwsWhenDownloadUrlMissing() {
		IllegalStateException error = assertThrows(IllegalStateException.class,
			() -> parser.parse("<html><p>Pay by </strong>15 Jan 2026</span><span>$1.00</span></html>"));

		assertTrue(error.getMessage().contains("bill download url"));
	}

}
