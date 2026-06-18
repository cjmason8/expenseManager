package au.com.mason.expensemanager.pdf.invoice;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.pdf.PdfExtractor;
import au.com.mason.expensemanager.pdf.PdfTextExtractor;

@Component
public class GlobirdInvoicePdfParser {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

	private final PdfTextExtractor pdfTextExtractor;

	public GlobirdInvoicePdfParser(PdfTextExtractor pdfTextExtractor) {
		this.pdfTextExtractor = pdfTextExtractor;
	}

	public GlobirdInvoiceData parse(byte[] pdfBytes) throws IOException {
		PdfExtractor pdf = PdfExtractor.from(pdfTextExtractor, pdfBytes);

		LocalDate issueDate = pdf.dateAtEndOfLineStartingWith("Issue Date", DATE_FORMAT)
			.orElseThrow(() -> missingField("Issue Date"));

		if (pdf.lineStartingWith("Due Date").isEmpty()) {
			return GlobirdInvoiceData.zeroCredit(issueDate);
		}

		LocalDate dueDate = pdf.dateAtEndOfLineStartingWith("Due Date", DATE_FORMAT)
			.orElseThrow(() -> missingField("Due Date"));
		String amount = pdf.lineAfterContaining("Amount Due").flatMap(line -> {
			String value = line.startsWith("$") ? line.substring(1) : line;
			return value.isBlank() ? java.util.Optional.empty() : java.util.Optional.of(value);
		}).orElseThrow(() -> missingField("Amount Due"));

		return new GlobirdInvoiceData(issueDate, dueDate, amount, false);
	}

	private IllegalStateException missingField(String field) {
		return new IllegalStateException("Globird invoice PDF missing: " + field);
	}

}
