package au.com.mason.expensemanager.html.wodongawater;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.html.BillNoticeData;
import au.com.mason.expensemanager.html.HtmlExtractor;

@Component
public class WodongaWaterBillHtmlParser {

	private static final int AMOUNT_OFFSET_AFTER_ZWNJ = 6;
	private static final int DUE_DATE_OFFSET_AFTER_ZWNJ = 5;

	private static final DateTimeFormatter DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy")
		.localizedBy(Locale.ENGLISH);

	public BillNoticeData parse(String html) {
		HtmlExtractor extractor = HtmlExtractor.fromText(html);

		String amount = extractor.textAfterZeroWidthMarker("Amount Due", AMOUNT_OFFSET_AFTER_ZWNJ)
			.orElseThrow(() -> missingField("amount"));
		var dueDate = extractor.textAfterZeroWidthMarker("Due Date", DUE_DATE_OFFSET_AFTER_ZWNJ)
			.map(value -> LocalDate.parse(value, DUE_DATE_FORMAT)).orElseThrow(() -> missingField("due date"));

		return new BillNoticeData(dueDate, amount);
	}

	private IllegalStateException missingField(String field) {
		return new IllegalStateException("Wodonga Water bill email missing: " + field);
	}

}
