package au.com.mason.expensemanager.pdf.rates;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.pdf.PdfExtractor;
import au.com.mason.expensemanager.pdf.PdfTextExtractor;

@Component
public class DingleyRatesInstalmentNoticePdfParser {

	private static final DateTimeFormatter DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy");

	private final PdfTextExtractor pdfTextExtractor;

	public DingleyRatesInstalmentNoticePdfParser(PdfTextExtractor pdfTextExtractor) {
		this.pdfTextExtractor = pdfTextExtractor;
	}

	public RatesInstalmentNoticeData parse(byte[] pdfBytes) throws IOException {
		PdfExtractor pdf = PdfExtractor.from(pdfTextExtractor, pdfBytes);

		return pdf.lineContaining("Total Amount Due")
			.map(line -> {
				String remainder = line.replace("Total Amount Due ", "");
				LocalDate dueDate = LocalDate.parse(remainder.substring(0, remainder.indexOf('$') - 1), DUE_DATE_FORMAT);
				String amount = remainder.substring(remainder.indexOf('$') + 1);
				return new RatesInstalmentNoticeData(dueDate, amount);
			})
			.orElseThrow(() -> new IllegalStateException("Dingley rates instalment PDF missing Total Amount Due"));
	}

}
