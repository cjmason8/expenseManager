package au.com.mason.expensemanager.html.lumo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.html.BillNoticeData;
import au.com.mason.expensemanager.html.HtmlExtractor;

@Component
public class LumoBillHtmlParser {

	private static final DateTimeFormatter DUE_DATE_FORMAT = new DateTimeFormatterBuilder().parseCaseInsensitive()
		.appendPattern("d MMM yy").toFormatter().localizedBy(Locale.ENGLISH);

	public BillNoticeData parse(String html) {
		HtmlExtractor extractor = HtmlExtractor.fromText(html);

		String amount = extractor.amountAfter("TOTAL AMOUNT DUE").orElseThrow(() -> missingField("amount"));
		var dueDate = extractor.textAfterAnchorFollowingLabel("DUE DATE", "white", 7)
			.map(value -> LocalDate.parse(value, DUE_DATE_FORMAT)).orElseThrow(() -> missingField("due date"));

		return new BillNoticeData(dueDate, amount);
	}

	private IllegalStateException missingField(String field) {
		return new IllegalStateException("Lumo bill email missing: " + field);
	}

}
