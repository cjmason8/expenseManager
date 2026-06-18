package au.com.mason.expensemanager.html.rates;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.html.BillNoticeData;
import au.com.mason.expensemanager.html.HtmlExtractor;

@Component
public class RatesInstalmentNoticeHtmlParser {

	private static final DateTimeFormatter DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy")
		.localizedBy(Locale.ENGLISH);

	public BillNoticeData parse(String html) {
		HtmlExtractor extractor = HtmlExtractor.fromText(html);

		var dueDate = extractor.textInCellAfterLabel("Due Date").map(value -> LocalDate.parse(value, DUE_DATE_FORMAT))
			.orElseThrow(() -> missingField("due date"));
		String amount = extractor.textInCellAfterLabel("Amount Due").orElseThrow(() -> missingField("amount due"));

		return new BillNoticeData(dueDate, amount);
	}

	private IllegalStateException missingField(String field) {
		return new IllegalStateException("Rates instalment notice email missing: " + field);
	}

}
