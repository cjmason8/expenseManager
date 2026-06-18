package au.com.mason.expensemanager.html.dingley;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.com.mason.expensemanager.html.rates.RatesFirstNoticeHtmlData;

class DingleyRatesHtmlParserTest {

	private DingleyRatesHtmlParser parser;

	@BeforeEach
	void setUp() {
		parser = new DingleyRatesHtmlParser();
	}

	@Test
	void extractPdfDownloadUrl_returnsLinkBeforeAnchor() {
		assertEquals("https://example.com/rates-instalment.pdf",
			parser.extractPdfDownloadUrl(DingleyRatesFixtures.INSTALMENT_EMAIL));
	}

	@Test
	void parseFirstNotice_extractsFirstInstalmentAmountAndYear() {
		RatesFirstNoticeHtmlData notice = parser.parseFirstNotice(DingleyRatesFixtures.FIRST_NOTICE_EMAIL);

		assertEquals("150.00", notice.firstInstalmentAmount());
		assertEquals(2025, notice.year());
	}

	@Test
	void extractPdfDownloadUrl_throwsWhenLinkMissing() {
		IllegalStateException error = assertThrows(IllegalStateException.class,
			() -> parser.extractPdfDownloadUrl("<html><p>No link</p></html>"));

		assertTrue(error.getMessage().contains("PDF download link"));
	}

}
