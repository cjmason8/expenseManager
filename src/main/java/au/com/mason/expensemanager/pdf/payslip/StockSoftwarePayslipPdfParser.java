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

	private static final Pattern ANNUAL_LEAVE_FULL_TIME_ROW = Pattern
		.compile("Annual\\s+Leave\\s*-\\s*Full\\s*Time", Pattern.CASE_INSENSITIVE);

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

		return new PayslipData(payToDate, extractAnnualLeaveFullTimeYtd(lines).orElse(null));
	}

	static Optional<BigDecimal> extractAnnualLeaveFullTimeYtd(List<String> lines) {
		Optional<Integer> ytdColumnIndex = findYtdColumnIndex(lines);
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (!ANNUAL_LEAVE_FULL_TIME_ROW.matcher(line).find()) {
				continue;
			}

			Optional<BigDecimal> fromColumns = valueFromColumns(line, ytdColumnIndex);
			if (fromColumns.isPresent()) {
				return fromColumns;
			}

			List<BigDecimal> numbers = extractDecimals(line);
			if (numbers.isEmpty() && i + 1 < lines.size()) {
				numbers = extractDecimals(lines.get(i + 1));
			}
			if (numbers.isEmpty()) {
				return Optional.empty();
			}

			final List<BigDecimal> rowNumbers = numbers;
			Optional<Integer> columnIndex = ytdColumnIndex;
			int index = columnIndex.filter(idx -> idx < rowNumbers.size()).orElse(rowNumbers.size() - 1);
			return Optional.of(rowNumbers.get(index));
		}
		return Optional.empty();
	}

	private static Optional<Integer> findYtdColumnIndex(List<String> lines) {
		for (String line : lines) {
			if (!line.toUpperCase(Locale.ENGLISH).contains("YTD")) {
				continue;
			}
			String[] parts = splitTableColumns(line);
			for (int i = 0; i < parts.length; i++) {
				if ("YTD".equalsIgnoreCase(parts[i].trim())) {
					return Optional.of(i);
				}
			}
		}
		return Optional.empty();
	}

	private static Optional<BigDecimal> valueFromColumns(String line, Optional<Integer> ytdColumnIndex) {
		String[] parts = splitTableColumns(line);
		if (parts.length < 2) {
			return Optional.empty();
		}
		int index = ytdColumnIndex.filter(idx -> idx > 0 && idx < parts.length).orElse(parts.length - 1);
		return parseDecimal(parts[index].trim());
	}

	private static String[] splitTableColumns(String line) {
		if (line.contains("  ")) {
			return line.trim().split("\\s{2,}");
		}
		return line.trim().split("\\s+");
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
