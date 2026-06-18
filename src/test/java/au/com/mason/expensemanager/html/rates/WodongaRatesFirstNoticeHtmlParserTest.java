package au.com.mason.expensemanager.html.rates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WodongaRatesFirstNoticeHtmlParserTest {

	private WodongaRatesFirstNoticeHtmlParser parser;

	@BeforeEach
	void setUp() {
		parser = new WodongaRatesFirstNoticeHtmlParser();
	}

	@Test
	void parse_extractsFirstInstalmentAmountAndYear() {
		RatesFirstNoticeHtmlData notice = parser.parse(WodongaRatesFirstNoticeFixtures.STANDARD_NOTICE);

		assertEquals("150.00", notice.firstInstalmentAmount());
		assertEquals(2025, notice.year());
	}

	@Test
	void parse_throwsWhenDueSectionMissing() {
		IllegalStateException error = assertThrows(IllegalStateException.class,
			() -> parser.parse("<html><p>No due section</p></html>"));

		assertTrue(error.getMessage().contains("Due section"));
	}

}
