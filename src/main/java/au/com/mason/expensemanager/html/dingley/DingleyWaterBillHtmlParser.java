package au.com.mason.expensemanager.html.dingley;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.html.BillNoticeData;
import au.com.mason.expensemanager.html.HtmlExtractor;

@Component
public class DingleyWaterBillHtmlParser {

	private static final int DATE_DUE_OFFSET = 26;
	private static final int DATE_CELL_OFFSET = 14;

	private static final DateTimeFormatter DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy")
		.localizedBy(Locale.ENGLISH);

	public BillNoticeData parse(String html) {
		HtmlExtractor extractor = HtmlExtractor.fromText(html.replace("<wbr>", ""));

		String amount = extractor.amountAfterFirstDollarSign()
			.orElseThrow(() -> missingField("amount"));
		var dueDate = extractor.textAfterAnchorFollowingLabel("Date due", DATE_DUE_OFFSET, "align=\"right\"",
				DATE_CELL_OFFSET)
			.map(value -> LocalDate.parse(value, DUE_DATE_FORMAT))
			.orElseThrow(() -> missingField("due date"));

		return new BillNoticeData(dueDate, amount);
	}

	private IllegalStateException missingField(String field) {
		return new IllegalStateException("Dingley Water bill email missing: " + field);
	}

}
