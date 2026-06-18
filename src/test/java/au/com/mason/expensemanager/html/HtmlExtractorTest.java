package au.com.mason.expensemanager.html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.junit.jupiter.api.Test;

class HtmlExtractorTest {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy")
		.localizedBy(Locale.ENGLISH);

	@Test
	void amountAfterHtmlEntity_extractsAmountBetweenEntityAndNbsp() {
		HtmlExtractor html = HtmlExtractor.fromText("Total &#36;123.45&#160;due");

		assertEquals("123.45", html.amountAfterHtmlEntity("&#36;").orElseThrow());
	}

	@Test
	void textAfterAnchorUntilTag_extractsValueBeforeNextTag() {
		HtmlExtractor html = HtmlExtractor.fromText("Debit on 15 Jun 2026</p>");

		assertEquals("15 Jun 2026", html.textAfterAnchorUntilTag("Debit on", 9).orElseThrow());
	}

	@Test
	void dateAfterAnchorUntilTag_parsesLocalDate() {
		HtmlExtractor html = HtmlExtractor.fromText("Debit on 15 Jun 2026</p>");

		assertEquals(LocalDate.of(2026, 6, 15), html.dateAfterAnchorUntilTag("Debit on", 9, DATE_FORMAT).orElseThrow());
	}

	@Test
	void textInCellAfterLabel_extractsRatesInstalmentValues() {
		HtmlExtractor html = HtmlExtractor.fromText("""
			<tr><td>Due Date</td><td class="value">15 March 2026</td></tr>
			<tr><td>Amount Due</td><td class="value">$500.00</td></tr>
			""");

		assertEquals("15 March 2026", html.textInCellAfterLabel("Due Date").orElseThrow());
		assertEquals("$500.00", html.textInCellAfterLabel("Amount Due").orElseThrow());
	}

	@Test
	void amountAfterDollarSign_extractsRacvStyleAmount() {
		HtmlExtractor html = HtmlExtractor.fromText("<td>$250.50</td>");

		assertEquals("250.50", html.amountAfterTag(">$").orElseThrow());
	}

	@Test
	void textAfterZeroWidthMarker_extractsWodongaWaterValues() {
		HtmlExtractor html = HtmlExtractor.fromText("""
			Amount Due zwnj $123.45</span>
			Due Date zwnj 15 March 2026</span>
			""");

		assertEquals("123.45", html.textAfterZeroWidthMarker("Amount Due", 6).orElseThrow());
		assertEquals("15 March 2026", html.textAfterZeroWidthMarker("Due Date", 5).orElseThrow());
	}

	@Test
	void urlBeforeStyleEnd_extractsBillDownloadLink() {
		HtmlExtractor html = HtmlExtractor.fromText("""
			<a href="https://example.com/bill.pdf" style="color:red">View my bill
			""");

		assertEquals("https://example.com/bill.pdf", html.urlBeforeStyleEnd("View my bill").orElseThrow());
	}

	@Test
	void textAfterAnchorFollowingLabel_extractsValueAfterLabelScopedAnchor() {
		HtmlExtractor html = HtmlExtractor.fromText("""
			<p>Due date</p>
			<span class="em_black_link">20 June 2026</span>
			""");

		assertEquals("20 June 2026", html.textAfterAnchorFollowingLabel("Due date", "em_black_link", 15).orElseThrow());
	}

	@Test
	void textAfterAnchorUntilTag_returnsEmptyWhenAnchorMissing() {
		HtmlExtractor html = HtmlExtractor.fromText("<p>No matching content</p>");

		assertTrue(html.textAfterAnchorUntilTag("Debit on", 9).isEmpty());
	}

}
