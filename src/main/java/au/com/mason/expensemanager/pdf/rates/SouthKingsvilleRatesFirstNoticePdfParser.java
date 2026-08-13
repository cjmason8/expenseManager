package au.com.mason.expensemanager.pdf.rates;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.pdf.PdfExtractor;
import au.com.mason.expensemanager.pdf.PdfTextExtractor;

@Component
public class SouthKingsvilleRatesFirstNoticePdfParser {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final Pattern DOLLAR_AMOUNT = Pattern.compile("\\$([\\d,]+(?:\\.\\d{2})?)");
	private static final Pattern DATE = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})");
	private static final Pattern RATING_PERIOD = Pattern.compile("(\\d{4})/(\\d{4})");

	private final PdfTextExtractor pdfTextExtractor;

	public SouthKingsvilleRatesFirstNoticePdfParser(PdfTextExtractor pdfTextExtractor) {
		this.pdfTextExtractor = pdfTextExtractor;
	}

	public List<RatesInstalmentData> parse(byte[] pdfBytes) throws IOException {
		PdfExtractor pdf = PdfExtractor.from(pdfTextExtractor, pdfBytes);
		List<String> lines = pdf.lines();

		String[] instalmentAmounts = extractInstalmentAmounts(lines);
		LocalDate[] dueDates = extractDueDates(lines);

		List<RatesInstalmentData> instalments = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			instalments.add(new RatesInstalmentData(dueDates[i], instalmentAmounts[i], i + 1));
		}
		return instalments;
	}

	private String[] extractInstalmentAmounts(List<String> lines) {
		for (int i = 0; i < lines.size() - 1; i++) {
			if (!lines.get(i).startsWith("Payments received after")) {
				continue;
			}

			List<String> amounts = dollarAmounts(lines.get(i + 1));
			if (amounts.size() >= 6) {
				// New Hobsons Bay layout: arrears, 1st, 2nd, 3rd, 4th, total
				return new String[]{amounts.get(1), amounts.get(2), amounts.get(3), amounts.get(4)};
			}
			if (amounts.size() == 4) {
				return amounts.toArray(String[]::new);
			}
			if (amounts.size() == 3) {
				// Legacy layout: 2nd/3rd/4th on amounts line, 1st on "1st Instalment" line
				String firstInstalment = findFirstInstalmentAmount(lines);
				return new String[]{firstInstalment, amounts.get(0), amounts.get(1), amounts.get(2)};
			}
		}

		throw new IllegalStateException("South Kingsville rates notice PDF missing instalment amounts");
	}

	private String findFirstInstalmentAmount(List<String> lines) {
		for (String line : lines) {
			if (!line.contains("1st Instalment")) {
				continue;
			}
			List<String> amounts = dollarAmounts(line);
			if (!amounts.isEmpty()) {
				return amounts.get(0);
			}
		}
		throw new IllegalStateException("South Kingsville rates notice PDF missing 1st instalment amount");
	}

	private LocalDate[] extractDueDates(List<String> lines) {
		// Prefer an explicit due-date line (new layout jumbles column order, so sort).
		for (String line : lines) {
			if (!line.contains("Due") || !DATE.matcher(line).find()) {
				continue;
			}
			List<LocalDate> dates = parseDates(line);
			if (dates.size() >= 4) {
				return sortedFour(dates);
			}
		}

		// Legacy layout: a line of four dd/MM/yyyy dates.
		for (String line : lines) {
			List<LocalDate> dates = parseDates(line);
			if (dates.size() == 4) {
				return dates.toArray(LocalDate[]::new);
			}
		}

		// Fallback: standard Hobsons Bay calendar dates + rating period year.
		Integer startYear = findRatingPeriodStartYear(lines);
		if (startYear != null) {
			return new LocalDate[]{LocalDate.of(startYear, 9, 30), LocalDate.of(startYear, 11, 30),
				LocalDate.of(startYear + 1, 2, 28), LocalDate.of(startYear + 1, 5, 31)};
		}

		throw new IllegalStateException("South Kingsville rates notice PDF missing instalment due dates");
	}

	private Integer findRatingPeriodStartYear(List<String> lines) {
		for (String line : lines) {
			Matcher matcher = RATING_PERIOD.matcher(line.trim());
			if (matcher.matches()) {
				return Integer.parseInt(matcher.group(1));
			}
		}
		for (String line : lines) {
			Matcher matcher = RATING_PERIOD.matcher(line);
			if (matcher.find()) {
				return Integer.parseInt(matcher.group(1));
			}
		}
		return null;
	}

	private List<String> dollarAmounts(String line) {
		List<String> amounts = new ArrayList<>();
		Matcher matcher = DOLLAR_AMOUNT.matcher(line);
		while (matcher.find()) {
			amounts.add("$" + matcher.group(1));
		}
		return amounts;
	}

	private List<LocalDate> parseDates(String line) {
		List<LocalDate> dates = new ArrayList<>();
		Matcher matcher = DATE.matcher(line);
		while (matcher.find()) {
			dates.add(LocalDate.parse(matcher.group(1), DATE_FORMAT));
		}
		return dates;
	}

	private LocalDate[] sortedFour(List<LocalDate> dates) {
		return dates.stream().distinct().sorted(Comparator.naturalOrder()).limit(4).toArray(LocalDate[]::new);
	}

}
