package au.com.mason.expensemanager.html.southkingsvillewater;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.html.BillDownloadNoticeData;
import au.com.mason.expensemanager.html.HtmlExtractor;

@Component
public class SouthKingsvilleWaterBillHtmlParser {

	private static final DateTimeFormatter DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd LLL yyyy")
		.localizedBy(Locale.ENGLISH);

	public BillDownloadNoticeData parse(String html) {
		HtmlExtractor extractor = HtmlExtractor.fromText(html);

		String downloadUrl = extractor.urlBeforeStyleEnd("View my bill")
			.orElseThrow(() -> missingField("bill download url"));
		String amount = extractor.amountAfterFirstDollarSign()
			.orElseThrow(() -> missingField("amount"));
		var dueDate = extractor.textInClosingSpanAfterAnchor("Pay by", 13)
			.map(value -> LocalDate.parse(value, DUE_DATE_FORMAT))
			.orElseThrow(() -> missingField("due date"));

		return new BillDownloadNoticeData(dueDate, amount, downloadUrl);
	}

	private IllegalStateException missingField(String field) {
		return new IllegalStateException("South Kingsville Water bill email missing: " + field);
	}

}
