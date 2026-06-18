package au.com.mason.expensemanager.html.telstra;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.html.BillNoticeData;
import au.com.mason.expensemanager.html.HtmlExtractor;

@Component
public class TelstraBillHtmlParser {

	private static final DateTimeFormatter DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy")
		.localizedBy(Locale.ENGLISH);

	public BillNoticeData parse(String html) {
		HtmlExtractor extractor = HtmlExtractor.fromText(html);

		String amount = extractor.amountAfterHtmlEntity("&#36;")
			.orElseThrow(() -> missingField("amount"));
		var dueDate = extractor.dateAfterAnchorUntilTag("Debit on", 9, DUE_DATE_FORMAT)
			.orElseThrow(() -> missingField("due date"));

		return new BillNoticeData(dueDate, amount);
	}

	private IllegalStateException missingField(String field) {
		return new IllegalStateException("Telstra bill email missing: " + field);
	}

}
