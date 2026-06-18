package au.com.mason.expensemanager.pdf.rental;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.pdf.PdfExtractor;
import au.com.mason.expensemanager.pdf.PdfTextExtractor;

@Component
public class SouthKingsvilleRentalStatementPdfParser {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final Pattern RENT_PAID_TO = Pattern.compile("Rent paid to (\\d{2}/\\d{2}/\\d{4})");
	private static final Pattern PREVIOUSLY_PAID_TO = Pattern.compile("previously paid to (\\d{2}/\\d{2}/\\d{4})");
	private static final Pattern MOVED_IN = Pattern.compile("moved in (\\d{2}/\\d{2}/\\d{4})");

	private final PdfTextExtractor pdfTextExtractor;

	public SouthKingsvilleRentalStatementPdfParser(PdfTextExtractor pdfTextExtractor) {
		this.pdfTextExtractor = pdfTextExtractor;
	}

	public RentalStatementData parse(byte[] pdfBytes) throws IOException {
		PdfExtractor pdf = PdfExtractor.from(pdfTextExtractor, pdfBytes);

		BigDecimal totalRent = pdf.amountFromLineStartingWith("Money In")
			.orElseThrow(() -> missingField("Money In"));
		BigDecimal managementFee = pdf.amountAfterAsteriskInLineContaining("Management fee")
			.orElseThrow(() -> missingField("Management fee"));
		BigDecimal adminFee = pdf.amountAfterAsteriskInLineContaining("Accounting Fee")
			.orElseThrow(() -> missingField("Accounting Fee"));
		BigDecimal paymentToOwner = pdf.amountFromLineStartingWith("You Received")
			.orElseThrow(() -> missingField("You Received"));

		String rentPeriodLine = pdf.lineContaining("Rent paid to")
			.orElseThrow(() -> missingField("Rent paid to"));
		LocalDate statementTo = pdf.dateMatching(rentPeriodLine, RENT_PAID_TO, DATE_FORMAT)
			.orElseThrow(() -> missingField("statement end date"));
		LocalDate statementFrom = parseStatementFrom(rentPeriodLine)
			.orElseThrow(() -> missingField("statement start date"));

		return new RentalStatementData(
			totalRent, managementFee, adminFee, statementFrom, statementTo, paymentToOwner);
	}

	private Optional<LocalDate> parseStatementFrom(String rentPeriodLine) {
		Pattern pattern = rentPeriodLine.contains("moved in") ? MOVED_IN : PREVIOUSLY_PAID_TO;
		return PdfExtractor.fromText(rentPeriodLine).dateMatching(rentPeriodLine, pattern, DATE_FORMAT);
	}

	private IllegalStateException missingField(String field) {
		return new IllegalStateException("South Kingsville rental statement PDF missing: " + field);
	}

}
