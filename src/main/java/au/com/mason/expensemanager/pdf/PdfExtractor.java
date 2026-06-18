package au.com.mason.expensemanager.pdf;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class PdfExtractor {

	private static final Pattern DOLLAR_AMOUNT = Pattern.compile("\\$([\\d,]+(?:\\.\\d+)?)");
	private static final Pattern DATE_DD_MM_YYYY = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})");

	private final String text;
	private final List<String> lines;

	private PdfExtractor(String text) {
		this.text = text;
		this.lines = List.of(text.split("\n"));
	}

	public static PdfExtractor fromBytes(byte[] pdfBytes) throws IOException {
		return fromText(PdfTextExtractor.extract(pdfBytes));
	}

	public static PdfExtractor from(PdfTextExtractor extractor, byte[] pdfBytes) throws IOException {
		return fromText(extractor.extractText(pdfBytes));
	}

	public static PdfExtractor fromText(String text) {
		return new PdfExtractor(text);
	}

	public String text() {
		return text;
	}

	public List<String> lines() {
		return lines;
	}

	public Stream<String> linesContaining(String fragment) {
		return lines.stream().filter(line -> line.contains(fragment));
	}

	public Optional<String> lineContaining(String fragment) {
		return linesContaining(fragment).findFirst();
	}

	public Optional<String> lineStartingWith(String prefix) {
		return lines.stream().filter(line -> line.startsWith(prefix)).findFirst();
	}

	public Optional<String> lineAfter(Predicate<String> matcher) {
		for (int i = 0; i < lines.size() - 1; i++) {
			if (matcher.test(lines.get(i))) {
				return Optional.of(lines.get(i + 1));
			}
		}
		return Optional.empty();
	}

	public Optional<String> lineAfterContaining(String fragment) {
		return lineAfter(line -> line.contains(fragment));
	}

	public Optional<BigDecimal> amountFromLine(String line) {
		Matcher matcher = DOLLAR_AMOUNT.matcher(line);
		if (!matcher.find()) {
			return Optional.empty();
		}
		return Optional.of(parseAmount(matcher.group(1)));
	}

	public Optional<BigDecimal> amountFromLineContaining(String fragment) {
		return lineContaining(fragment).flatMap(this::amountFromLine);
	}

	public Optional<BigDecimal> amountFromLineStartingWith(String prefix) {
		return lineStartingWith(prefix).flatMap(this::amountFromLine);
	}

	public Optional<BigDecimal> amountAfterAsteriskInLineContaining(String fragment) {
		return lineContaining(fragment).flatMap(line -> {
			int asteriskIndex = line.indexOf('*');
			if (asteriskIndex == -1) {
				return Optional.empty();
			}
			return amountFromLine(line.substring(asteriskIndex));
		});
	}

	public Optional<LocalDate> firstDateInLineContaining(String fragment, DateTimeFormatter formatter) {
		return lineContaining(fragment).flatMap(line -> firstDateInLine(line, formatter));
	}

	public Optional<LocalDate> firstDateInLine(String line, DateTimeFormatter formatter) {
		Matcher matcher = DATE_DD_MM_YYYY.matcher(line);
		if (!matcher.find()) {
			return Optional.empty();
		}
		return Optional.of(LocalDate.parse(matcher.group(1), formatter));
	}

	public Optional<LocalDate> dateMatching(String line, Pattern pattern, DateTimeFormatter formatter) {
		Matcher matcher = pattern.matcher(line);
		if (!matcher.find()) {
			return Optional.empty();
		}
		return Optional.of(LocalDate.parse(matcher.group(1), formatter));
	}

	public Optional<LocalDate> dateAtEndOfLineStartingWith(String prefix, DateTimeFormatter formatter) {
		return lineStartingWith(prefix).map(line -> {
			String datePart = line.substring(line.lastIndexOf(' ') + 1);
			return LocalDate.parse(datePart, formatter);
		});
	}

	public Optional<BigDecimal> amountFromFollowingLineContaining(String fragment) {
		return lineAfterContaining(fragment).flatMap(this::amountFromLine);
	}

	private static BigDecimal parseAmount(String amount) {
		return new BigDecimal(amount.replace(",", ""));
	}

}
