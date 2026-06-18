package au.com.mason.expensemanager.html.dingley;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.html.HtmlExtractor;
import au.com.mason.expensemanager.html.rates.RatesFirstNoticeHtmlData;

@Component
public class DingleyRatesHtmlParser {

	public String extractPdfDownloadUrl(String html) {
		return HtmlExtractor.fromText(html).urlBeforeAnchor("Click here to follow link")
			.orElseThrow(() -> new IllegalStateException("Dingley rates email missing: PDF download link"));
	}

	public RatesFirstNoticeHtmlData parseFirstNotice(String html) {
		HtmlExtractor extractor = HtmlExtractor.fromText(html);

		String firstInstalmentAmount = extractor.amountAfterHtmlEntity("&#36;")
			.orElseThrow(() -> missingField("first instalment amount"));
		int year = extractYear(html);

		return new RatesFirstNoticeHtmlData(firstInstalmentAmount, year);
	}

	private int extractYear(String html) {
		int year = 2018;
		while (html.indexOf("&#47;" + year + "</span>") == -1) {
			year++;
			if (year > 2100) {
				throw missingField("year");
			}
		}
		return year;
	}

	private IllegalStateException missingField(String field) {
		return new IllegalStateException("Dingley rates first notice email missing: " + field);
	}

}
