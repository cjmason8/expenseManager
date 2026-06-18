package au.com.mason.expensemanager.pdf.rental;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.pdf.PdfExtractor;
import au.com.mason.expensemanager.pdf.PdfTextExtractor;

@Component
public class WodongaRentalStatementPdfParser {

	private static final DateTimeFormatter STATEMENT_PERIOD_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy");

	private final PdfTextExtractor pdfTextExtractor;

	public WodongaRentalStatementPdfParser(PdfTextExtractor pdfTextExtractor) {
		this.pdfTextExtractor = pdfTextExtractor;
	}

	public RentalStatementData parse(byte[] pdfBytes) throws IOException {
		PdfExtractor pdf = PdfExtractor.from(pdfTextExtractor, pdfBytes);

		BigDecimal totalRent = null;
		BigDecimal managementFee = null;
		BigDecimal adminFee = null;
		BigDecimal paymentToOwner = null;
		LocalDate statementFrom = null;
		LocalDate statementTo = null;
		boolean awaitingAdminFee = false;
		boolean awaitingPaymentToOwner = false;

		for (String line : pdf.lines()) {
			if (line.contains("Total\tincome")) {
				totalRent = amountFromLine(line);
			} else if (line.contains("Rent\tCommission")) {
				managementFee = amountFromLine(line);
			} else if (line.contains("Sundry\tFee")) {
				if (line.indexOf('$') == -1) {
					awaitingAdminFee = true;
				} else {
					adminFee = amountFromLine(line);
				}
			} else if (awaitingAdminFee) {
				adminFee = amountFromLine(line);
				awaitingAdminFee = false;
			} else if (line.contains("Payment\tto\towner")) {
				awaitingPaymentToOwner = true;
			} else if (line.startsWith("Statement\tperiod")) {
				String[] dates = line.replace("Statement\tperiod ", "").split("\t-\t");
				statementFrom = LocalDate.parse(dates[0].replace("\t", " "), STATEMENT_PERIOD_FORMAT);
				statementTo = LocalDate.parse(dates[1].replace("\t", " "), STATEMENT_PERIOD_FORMAT);
			} else if (awaitingPaymentToOwner) {
				paymentToOwner = amountFromLine(line);
				awaitingPaymentToOwner = false;
			}
		}

		if (totalRent == null || managementFee == null || adminFee == null || paymentToOwner == null
			|| statementFrom == null || statementTo == null) {
			throw new IllegalStateException("Wodonga rental statement PDF missing required fields");
		}

		return new RentalStatementData(totalRent, managementFee, adminFee, statementFrom, statementTo, paymentToOwner);
	}

	private BigDecimal amountFromLine(String line) {
		return PdfExtractor.fromText(line).amountFromLine(line).orElseThrow(
			() -> new IllegalStateException("Wodonga rental statement PDF amount missing on line: " + line));
	}

}
