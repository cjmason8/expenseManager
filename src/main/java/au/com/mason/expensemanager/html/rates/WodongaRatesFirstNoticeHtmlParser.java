package au.com.mason.expensemanager.html.rates;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.html.HtmlExtractor;

@Component
public class WodongaRatesFirstNoticeHtmlParser {

	public RatesFirstNoticeHtmlData parse(String html) {
		int dueIndex = html.indexOf("Due");
		if (dueIndex == -1) {
			throw new IllegalStateException("Wodonga rates first notice email missing: Due section");
		}

		HtmlExtractor extractor = HtmlExtractor.fromText(html.substring(dueIndex));
		String firstInstalmentAmount = extractor.amountAfterFirstDollarSign()
			.orElseThrow(() -> missingField("first instalment amount"));
		int year = extractor.yearAfterSpaceBefore20().orElseThrow(() -> missingField("year"));

		return new RatesFirstNoticeHtmlData(firstInstalmentAmount, year);
	}

	private IllegalStateException missingField(String field) {
		return new IllegalStateException("Wodonga rates first notice email missing: " + field);
	}

}
