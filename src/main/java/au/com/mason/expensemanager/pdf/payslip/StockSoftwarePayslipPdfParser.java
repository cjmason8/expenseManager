package au.com.mason.expensemanager.pdf.payslip;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.pdf.PdfExtractor;
import au.com.mason.expensemanager.pdf.PdfTextExtractor;

@Component
public class StockSoftwarePayslipPdfParser {

	private static final Pattern PAY_TO_DATE = Pattern
		.compile("Pay\\s*(?:To\\s*)?Date\\s*[:\\-]?\\s*(\\d{1,2}[\\-/]\\d{1,2}[\\-/]\\d{4})", Pattern.CASE_INSENSITIVE);

	private static final Pattern PAYMENT_DATE = Pattern
		.compile("Payment\\s*Date\\s*[:\\-]?\\s*(\\d{1,2}[\\-/]\\d{1,2}[\\-/]\\d{4})", Pattern.CASE_INSENSITIVE);

	// Matches the row label and any arrangement suffix, e.g. "Annual Leave -
	// FullTime",
	// "Annual Leave - 9/10 Time". Matching the label lets us strip it before
	// reading
	// numbers, so digits in a suffix like "9/10" are not mistaken for table values.
	private static final Pattern ANNUAL_LEAVE_ROW_LABEL = Pattern.compile(
		"Annual\\s+Leave\\s*-\\s*(?:\\d+(?:\\s*/\\s*\\d+|\\.\\d+)?\\s*)?[A-Za-z][A-Za-z\\s]*",
		Pattern.CASE_INSENSITIVE);

	private static final Pattern DECIMAL = Pattern.compile("\\d+(?:\\.\\d+)?");

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
		List<String> lines = pdf.lines();

		LocalDate payToDate = firstMatch(PAY_TO_DATE, text).or(() -> firstMatch(PAYMENT_DATE, text))
			.or(() -> pdf.lineContaining("Pay To Date").flatMap(this::parseDateFromLine))
			.or(() -> pdf.lineContaining("Pay To").flatMap(this::parseDateFromLine))
			.or(() -> pdf.lineContaining("Payment Date").flatMap(this::parseDateFromLine))
			.orElseThrow(() -> new IllegalStateException("Stock Software payslip PDF missing pay to date"));

		return new PayslipData(payToDate, extractAnnualLeaveYtd(lines).orElse(null));
	}

	static Optional<BigDecimal> extractAnnualLeaveYtd(List<String> lines) {
		BigDecimal total = null;
		for (int i = 0; i < lines.size(); i++) {
			if (!ANNUAL_LEAVE_ROW_LABEL.matcher(lines.get(i)).find()) {
				continue;
			}

			Optional<BigDecimal> rowYtd = rowYtdValue(lines, i);
			if (rowYtd.isPresent()) {
				total = total == null ? rowYtd.get() : total.add(rowYtd.get());
			}
		}
		return Optional.ofNullable(total);
	}

	/**
	 * Reads the YTD figure for a single leave row. Leave rows populate a varying number of
	 * columns (an arrangement no longer accruing shows YTD only), so the value is taken as
	 * the last number on the row rather than by column position. The trailing TYPE column
	 * is always a word, so YTD is the final numeric value.
	 */
	private static Optional<BigDecimal> rowYtdValue(List<String> lines, int lineIndex) {
		List<BigDecimal> numbers = extractDecimals(stripLeaveTypeLabel(lines.get(lineIndex)));
		if (numbers.isEmpty() && lineIndex + 1 < lines.size()) {
			String nextLine = lines.get(lineIndex + 1);
			// Only borrow the next line when it is not itself a leave row, otherwise a
			// label-only row would consume the following row's values and double count.
			if (!ANNUAL_LEAVE_ROW_LABEL.matcher(nextLine).find()) {
				numbers = extractDecimals(nextLine);
			}
		}
		if (numbers.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(numbers.get(numbers.size() - 1));
	}

	private static String stripLeaveTypeLabel(String line) {
		Matcher matcher = ANNUAL_LEAVE_ROW_LABEL.matcher(line);
		if (matcher.find()) {
			return line.substring(matcher.end());
		}
		return line;
	}

	private static List<BigDecimal> extractDecimals(String line) {
		List<BigDecimal> numbers = new ArrayList<>();
		Matcher matcher = DECIMAL.matcher(line);
		while (matcher.find()) {
			parseDecimal(matcher.group()).ifPresent(numbers::add);
		}
		return numbers;
	}

	private static Optional<BigDecimal> parseDecimal(String token) {
		try {
			return Optional.of(new BigDecimal(token.replace(",", "")));
		} catch (NumberFormatException ignored) {
			return Optional.empty();
		}
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
		for (DateTimeFormatter formatter : new DateTimeFormatter[]{SLASH_DATE, DASH_DATE, TEXT_DATE}) {
			try {
				return Optional.of(LocalDate.parse(token.trim(), formatter));
			} catch (DateTimeParseException ignored) {
				// try next format
			}
		}
		return Optional.empty();
	}

}
