package au.com.mason.expensemanager.pdf.payslip;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.pdf.PdfExtractor;
import au.com.mason.expensemanager.pdf.PdfTextExtractor;

@Component
public class StockSoftwarePayslipPdfParser {

	private static final Pattern PAY_TO_DATE = Pattern.compile(
		"Pay\\s*(?:To\\s*)?Date\\s*[:\\-]?\\s*(\\d{1,2}[\\-/]\\d{1,2}[\\-/]\\d{4})",
		Pattern.CASE_INSENSITIVE);

	private static final Pattern PAYMENT_DATE = Pattern.compile(
		"Payment\\s*Date\\s*[:\\-]?\\s*(\\d{1,2}[\\-/]\\d{1,2}[\\-/]\\d{4})",
		Pattern.CASE_INSENSITIVE);

	private static final DateTimeFormatter SLASH_DATE = DateTimeFormatter.ofPattern("d/M/yyyy");

	private static final DateTimeFormatter DASH_DATE = DateTimeFormatter.ofPattern("d-M-yyyy");

	private static final DateTimeFormatter TEXT_DATE = new DateTimeFormatterBuilder().parseCaseInsensitive()
		.appendPattern("d MMMM yyyy").toFormatter(Locale.ENGLISH);

	private final PdfTextExtractor pdfTextExtractor;

	public StockSoftwarePayslipPdfParser(PdfTextExtractor pdfTextExtractor) {
		this.pdfTextExtractor = pdfTextExtractor;
	}

	public PayslipData parse(byte[] pdfBytes) throws IOException {
		PdfExtractor pdf = PdfExtractor.from(pdfTextExtractor, pdfBytes);
		String text = pdf.text();

		LocalDate payToDate = firstMatch(PAY_TO_DATE, text).or(() -> firstMatch(PAYMENT_DATE, text))
			.or(() -> pdf.lineContaining("Pay To Date").flatMap(this::parseDateFromLine))
			.or(() -> pdf.lineContaining("Pay To").flatMap(this::parseDateFromLine))
			.or(() -> pdf.lineContaining("Payment Date").flatMap(this::parseDateFromLine))
			.orElseThrow(() -> new IllegalStateException("Stock Software payslip PDF missing pay to date"));

		return new PayslipData(payToDate);
	}

	private Optional<LocalDate> firstMatch(Pattern pattern, String text) {
		Matcher matcher = pattern.matcher(text);
		if (!matcher.find()) {
			return Optional.empty();
		}
		return parseDateToken(matcher.group(1));
	}

	private Optional<LocalDate> parseDateFromLine(String line) {
		Matcher matcher = Pattern.compile("(\\d{1,2}[\\-/]\\d{1,2}[\\-/]\\d{4})").matcher(line);
		if (matcher.find()) {
			return parseDateToken(matcher.group(1));
		}
		matcher = Pattern.compile("(\\d{1,2}\\s+[A-Za-z]+\\s+\\d{4})").matcher(line);
		if (matcher.find()) {
			return parseDateToken(matcher.group(1));
		}
		return Optional.empty();
	}

	private Optional<LocalDate> parseDateToken(String token) {
		for (DateTimeFormatter formatter : new DateTimeFormatter[] { SLASH_DATE, DASH_DATE, TEXT_DATE }) {
			try {
				return Optional.of(LocalDate.parse(token.trim(), formatter));
			} catch (DateTimeParseException ignored) {
				// try next format
			}
		}
		return Optional.empty();
	}

}
