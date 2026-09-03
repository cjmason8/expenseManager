package au.com.mason.expensemanager.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DocumentFileNameDates {

	private static final Pattern DASHED_DATE = Pattern.compile("(\\d{1,2})-(\\d{1,2})-(\\d{4})");

	private static final Pattern COMPACT_DATE = Pattern.compile("(?<!\\d)(\\d{2})(\\d{2})(\\d{4})(?!\\d)");

	private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("d-M-uuuu")
		.withResolverStyle(ResolverStyle.STRICT);

	private static final DateTimeFormatter COMPACT_DMY = DateTimeFormatter.ofPattern("ddMMuuuu")
		.withResolverStyle(ResolverStyle.STRICT);

	private DocumentFileNameDates() {
	}

	public static Optional<LocalDate> extractDate(String fileName) {
		if (fileName == null || fileName.isBlank()) {
			return Optional.empty();
		}
		Optional<LocalDate> dashed = extractDashedDate(fileName);
		if (dashed.isPresent()) {
			return dashed;
		}
		return extractCompactDate(fileName);
	}

	private static Optional<LocalDate> extractDashedDate(String fileName) {
		Matcher matcher = DASHED_DATE.matcher(fileName);
		LocalDate latest = null;
		while (matcher.find()) {
			Optional<LocalDate> parsed = parseDashed(matcher.group(1), matcher.group(2), matcher.group(3));
			if (parsed.isPresent() && (latest == null || parsed.get().isAfter(latest))) {
				latest = parsed.get();
			}
		}
		return Optional.ofNullable(latest);
	}

	private static Optional<LocalDate> extractCompactDate(String fileName) {
		Matcher matcher = COMPACT_DATE.matcher(fileName);
		LocalDate latest = null;
		while (matcher.find()) {
			Optional<LocalDate> parsed = parseCompact(matcher.group(1), matcher.group(2), matcher.group(3));
			if (parsed.isPresent() && (latest == null || parsed.get().isAfter(latest))) {
				latest = parsed.get();
			}
		}
		return Optional.ofNullable(latest);
	}

	private static Optional<LocalDate> parseDashed(String day, String month, String year) {
		try {
			return Optional.of(LocalDate.parse(day + "-" + month + "-" + year, DMY));
		} catch (DateTimeParseException ignored) {
			return Optional.empty();
		}
	}

	private static Optional<LocalDate> parseCompact(String day, String month, String year) {
		try {
			return Optional.of(LocalDate.parse(day + month + year, COMPACT_DMY));
		} catch (DateTimeParseException ignored) {
			return Optional.empty();
		}
	}

}
