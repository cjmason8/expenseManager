package au.com.mason.expensemanager.html.racv;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.html.BillNoticeData;
import au.com.mason.expensemanager.html.HtmlExtractor;

@Component
public class RacvBillHtmlParser {

	private static final DateTimeFormatter DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy")
		.localizedBy(Locale.ENGLISH);

	public BillNoticeData parse(String html) {
		HtmlExtractor extractor = HtmlExtractor.fromText(html);

		String amount = extractor.amountAfterTag(">$").orElseThrow(() -> missingField("amount"));
		var dueDate = extractor.dateAfterAnchorUntilTag(">Due", 5, DUE_DATE_FORMAT)
			.orElseThrow(() -> missingField("due date"));

		return new BillNoticeData(dueDate, amount);
	}

	private IllegalStateException missingField(String field) {
		return new IllegalStateException("RACV bill email missing: " + field);
	}

}
