package au.com.mason.expensemanager.pdf.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.pdf.PdfTextExtractor;

/**
 * Finds the invoice total (inc. GST) on maintenance invoices from arbitrary
 * trades. Returns {@link #UNKNOWN} when no total can be found or when the
 * balance due disagrees with the total in a way that payments don't explain
 * (e.g. a shared fence where only part of the job is owed).
 */
@Component
public class MaintenanceInvoiceTotalExtractor {

	public static final String TOTAL_KEY = "Total";
	public static final String UNKNOWN = "UNKNOWN";

	private static final Logger LOGGER = LogManager.getLogger(MaintenanceInvoiceTotalExtractor.class);

	private static final int LOOKAHEAD_LINES = 3;

	/** Joins letter-spaced words such as {@code T o t a l}. */
	private static final Pattern LETTER_SPACING = Pattern.compile("(?<=\\b[A-Za-z]) (?=[A-Za-z]\\b)");

	private static final Pattern MONEY = Pattern
		.compile("(?<![\\w.,])(-)?(?:A?\\$|AUD)?\\s?(\\d{1,3}(?:,\\d{3})+|\\d+)\\.(\\d{2})(?![\\d%])"
			+ "|(?<![\\w.,])(-)?A?\\$\\s?(\\d{1,3}(?:,\\d{3})+|\\d+)(?![\\d.,%])");

	private static final Pattern FOLLOWED_BY_AMOUNT = Pattern
		.compile("^\\s*[:=]?\\s*(?:AUD\\b)?\\s*(?:-?A?\\$)?\\s*(?:\\d|$)", Pattern.CASE_INSENSITIVE);

	private static final Pattern SPECIFIC_TOTAL = Pattern.compile("grand\\s*total|invoice\\s*total|total\\s*aud"
		+ "|total\\s*inc(?:l(?:uding|\\.)?|\\.)?\\s*gst|total\\s*charge|total\\s*amount"
		+ "|job\\s*total(?:\\s*including\\s*gst)?|total\\s*due", Pattern.CASE_INSENSITIVE);

	private static final Pattern GENERIC_TOTAL = Pattern.compile("(?<![a-z])total\\b:?", Pattern.CASE_INSENSITIVE);

	private static final Pattern BALANCE_DUE = Pattern.compile("balance\\s*due|amount\\s*due|please\\s*pay",
		Pattern.CASE_INSENSITIVE);

	private static final Pattern PAID = Pattern.compile("\\b(?:amount\\s*|total\\s*)?paid\\b:?",
		Pattern.CASE_INSENSITIVE);

	private static final Pattern NOT_A_TOTAL = Pattern
		.compile("sub\\s*-?\\s*total|total\\s*(?:tax|gst|exclusive|excl|discount|paid|net)|gst\\s*total|includes\\s*gst"
			+ "|quantity|description|unit\\s*price|\\bqty\\b", Pattern.CASE_INSENSITIVE);

	private final PdfTextExtractor pdfTextExtractor;

	public MaintenanceInvoiceTotalExtractor(PdfTextExtractor pdfTextExtractor) {
		this.pdfTextExtractor = pdfTextExtractor;
	}

	/**
	 * @return the total formatted like {@code 379.00}, or {@link #UNKNOWN}
	 */
	public String extractTotal(byte[] content) {
		if (!isPdf(content)) {
			return UNKNOWN;
		}
		try {
			String total = totalFromText(pdfTextExtractor.extractText(content));
			if (UNKNOWN.equals(total)) {
				total = totalFromText(pdfTextExtractor.extractTextSortedByPosition(content));
			}
			return total;
		} catch (Exception e) {
			LOGGER.warn("Unable to read maintenance invoice total", e);
			return UNKNOWN;
		}
	}

	static String totalFromText(String text) {
		if (text == null || text.isBlank()) {
			return UNKNOWN;
		}
		String[] lines = LETTER_SPACING.matcher(text.replaceAll("[^\\x09\\x0A\\x0D\\x20-\\x7E]", " ")).replaceAll("")
			.split("\\r?\\n");

		Optional<BigDecimal> due = bestAmount(lines, BALANCE_DUE, false);
		Optional<BigDecimal> total = bestAmount(lines, SPECIFIC_TOTAL, true);
		if (total.isEmpty()) {
			total = bestAmount(lines, GENERIC_TOTAL, true);
		}
		if (total.isEmpty()) {
			total = due;
		}
		if (total.isEmpty()) {
			return UNKNOWN;
		}

		if (due.isPresent() && total.get().compareTo(due.get()) != 0) {
			BigDecimal paid = bestAmount(lines, PAID, false).orElse(BigDecimal.ZERO);
			if (total.get().subtract(paid).compareTo(due.get()) != 0) {
				return UNKNOWN;
			}
		}
		return total.get().setScale(2, RoundingMode.HALF_UP).toPlainString();
	}

	static boolean isPdf(byte[] content) {
		if (content == null || content.length < 4) {
			return false;
		}
		String head = new String(content, 0, Math.min(content.length, 1024), StandardCharsets.ISO_8859_1);
		return head.contains("%PDF");
	}

	/**
	 * Amounts on the same line as the label win over amounts found on the following
	 * lines; within each group the largest amount wins.
	 */
	private static Optional<BigDecimal> bestAmount(String[] lines, Pattern label, boolean isTotal) {
		List<BigDecimal> sameLine = new ArrayList<>();
		List<BigDecimal> following = new ArrayList<>();
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (isTotal && NOT_A_TOTAL.matcher(line).find()) {
				continue;
			}
			Matcher matcher = label.matcher(line);
			while (matcher.find()) {
				String after = line.substring(matcher.end());
				if (!FOLLOWED_BY_AMOUNT.matcher(after).find()) {
					continue;
				}
				Optional<BigDecimal> amount = firstAmount(after)
					.or(() -> lastAmount(line.substring(0, matcher.start())));
				if (amount.isPresent()) {
					sameLine.add(amount.get());
				} else {
					amountOnFollowingLines(lines, i).ifPresent(following::add);
				}
				break;
			}
		}
		if (!sameLine.isEmpty()) {
			return Optional.of(Collections.max(sameLine));
		}
		return following.isEmpty() ? Optional.empty() : Optional.of(Collections.max(following));
	}

	private static Optional<BigDecimal> amountOnFollowingLines(String[] lines, int labelLine) {
		for (int j = labelLine + 1; j < Math.min(lines.length, labelLine + 1 + LOOKAHEAD_LINES); j++) {
			if (NOT_A_TOTAL.matcher(lines[j]).find()) {
				return Optional.empty();
			}
			Optional<BigDecimal> amount = firstAmount(lines[j]);
			if (amount.isPresent()) {
				return amount;
			}
		}
		return Optional.empty();
	}

	private static Optional<BigDecimal> firstAmount(String text) {
		Matcher matcher = MONEY.matcher(text);
		return matcher.find() ? Optional.of(toAmount(matcher)) : Optional.empty();
	}

	private static Optional<BigDecimal> lastAmount(String text) {
		Matcher matcher = MONEY.matcher(text);
		BigDecimal last = null;
		while (matcher.find()) {
			last = toAmount(matcher);
		}
		return Optional.ofNullable(last);
	}

	private static BigDecimal toAmount(Matcher matcher) {
		boolean withCents = matcher.group(2) != null;
		String sign = withCents ? matcher.group(1) : matcher.group(4);
		String whole = (withCents ? matcher.group(2) : matcher.group(5)).replace(",", "");
		String cents = withCents ? matcher.group(3) : "00";
		BigDecimal amount = new BigDecimal(whole + "." + cents);
		return sign == null ? amount : amount.negate();
	}

}
