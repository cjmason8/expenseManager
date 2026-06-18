package au.com.mason.expensemanager.html;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HtmlExtractor {

	private static final Pattern HREF_URL = Pattern.compile("href=\"(https?://[^\"]+)\"");

	private final String html;

	private HtmlExtractor(String html) {
		this.html = html;
	}

	public static HtmlExtractor fromText(String html) {
		return new HtmlExtractor(html);
	}

	public String html() {
		return html;
	}

	public int indexOf(String fragment) {
		return html.indexOf(fragment);
	}

	public Optional<String> textFromOffsetUntilTag(int startIndex) {
		if (startIndex < 0) {
			return Optional.empty();
		}
		int endIndex = html.indexOf('<', startIndex);
		if (endIndex == -1) {
			return Optional.empty();
		}
		return Optional.of(html.substring(startIndex, endIndex).trim());
	}

	public Optional<String> textAfterAnchorUntilTag(String anchor, int offsetFromAnchorStart) {
		int anchorIndex = html.indexOf(anchor);
		if (anchorIndex == -1) {
			return Optional.empty();
		}
		return textFromOffsetUntilTag(anchorIndex + offsetFromAnchorStart);
	}

	public Optional<String> textAfterAnchorFollowingLabel(String label, String anchor, int offsetFromAnchorStart) {
		return textAfterAnchorFollowingLabel(label, 0, anchor, offsetFromAnchorStart);
	}

	public Optional<String> textAfterAnchorFollowingLabel(String label, int offsetAfterLabel, String anchor,
		int offsetFromAnchorStart) {
		int labelIndex = html.indexOf(label);
		if (labelIndex == -1) {
			return Optional.empty();
		}
		int anchorIndex = html.indexOf(anchor, labelIndex + offsetAfterLabel);
		if (anchorIndex == -1) {
			return Optional.empty();
		}
		return textFromOffsetUntilTag(anchorIndex + offsetFromAnchorStart);
	}

	public Optional<String> textBetween(int startInclusive, int endExclusive) {
		if (startInclusive < 0 || endExclusive < startInclusive) {
			return Optional.empty();
		}
		return Optional.of(html.substring(startInclusive, endExclusive).trim());
	}

	public Optional<String> textBetweenMarkers(String startMarker, int startOffset, String endMarker, int searchFrom) {
		int startMarkerIndex = html.indexOf(startMarker, searchFrom);
		if (startMarkerIndex == -1) {
			return Optional.empty();
		}
		int startIndex = startMarkerIndex + startOffset;
		int endIndex = html.indexOf(endMarker, startIndex);
		if (endIndex == -1) {
			return Optional.empty();
		}
		return Optional.of(html.substring(startIndex, endIndex).trim());
	}

	public Optional<String> textInCellAfterLabel(String label) {
		int labelIndex = html.indexOf(label);
		if (labelIndex == -1) {
			return Optional.empty();
		}
		int cellStart = html.indexOf("\">", labelIndex);
		if (cellStart == -1) {
			return Optional.empty();
		}
		return textFromOffsetUntilTag(cellStart + 2);
	}

	public Optional<String> amountAfterHtmlEntity(String entity) {
		return textBetweenMarkers(entity, entity.length(), "&#160;", html.indexOf(entity));
	}

	public Optional<String> amountAfterTag(String marker) {
		int markerIndex = html.indexOf(marker);
		if (markerIndex == -1) {
			return Optional.empty();
		}
		return textFromOffsetUntilTag(markerIndex + marker.length());
	}

	public Optional<String> amountAfterDollarSign() {
		int markerIndex = html.indexOf(">$");
		if (markerIndex == -1) {
			return Optional.empty();
		}
		return textFromOffsetUntilTag(markerIndex + 2);
	}

	public Optional<String> amountAfterFirstDollarSign() {
		int dollarIndex = html.indexOf('$');
		if (dollarIndex == -1) {
			return Optional.empty();
		}
		return textFromOffsetUntilTag(dollarIndex + 1);
	}

	public Optional<Integer> yearAfterSpaceBefore20() {
		int yearIndex = html.indexOf(" 20");
		if (yearIndex == -1) {
			return Optional.empty();
		}
		return textFromOffsetUntilTag(yearIndex + 1).map(Integer::parseInt);
	}

	public Optional<String> textInClosingSpanAfterAnchor(String anchor, int offsetFromAnchorStart) {
		int anchorIndex = html.indexOf(anchor);
		if (anchorIndex == -1) {
			return Optional.empty();
		}
		int contentStart = anchorIndex + offsetFromAnchorStart;
		int spanEnd = html.indexOf("</span", contentStart);
		if (spanEnd == -1) {
			return Optional.empty();
		}
		String spanContent = html.substring(contentStart, spanEnd);
		int lastGreaterThan = spanContent.lastIndexOf('>');
		if (lastGreaterThan == -1) {
			return Optional.empty();
		}
		return Optional.of(spanContent.substring(lastGreaterThan + 1).trim());
	}

	public Optional<String> amountAfter(String anchor) {
		int anchorIndex = html.indexOf(anchor);
		if (anchorIndex == -1) {
			return Optional.empty();
		}
		int dollarIndex = html.indexOf('$', anchorIndex);
		if (dollarIndex == -1) {
			return Optional.empty();
		}
		return textFromOffsetUntilTag(dollarIndex);
	}

	public Optional<String> textAfterZeroWidthMarker(String label, int charsAfterMarker) {
		int labelIndex = html.indexOf(label);
		if (labelIndex == -1) {
			return Optional.empty();
		}
		int markerIndex = html.indexOf("zwnj", labelIndex);
		if (markerIndex == -1) {
			return Optional.empty();
		}
		return textFromOffsetUntilTag(markerIndex + charsAfterMarker);
	}

	public Optional<LocalDate> dateAfterAnchorUntilTag(String anchor, int offsetFromAnchorStart,
		DateTimeFormatter formatter) {
		return textAfterAnchorUntilTag(anchor, offsetFromAnchorStart).map(value -> LocalDate.parse(value, formatter));
	}

	public Optional<LocalDate> dateAfterAnchorFollowingLabel(String label, String anchor, int offsetFromAnchorStart,
		DateTimeFormatter formatter) {
		return textAfterAnchorFollowingLabel(label, anchor, offsetFromAnchorStart)
			.map(value -> LocalDate.parse(value, formatter));
	}

	public Optional<String> urlBeforeAnchor(String anchor) {
		int anchorIndex = html.indexOf(anchor);
		if (anchorIndex == -1) {
			return Optional.empty();
		}
		String prefix = html.substring(0, anchorIndex);
		Matcher matcher = HREF_URL.matcher(prefix);
		String lastUrl = null;
		while (matcher.find()) {
			lastUrl = matcher.group(1);
		}
		return Optional.ofNullable(lastUrl);
	}

	public Optional<String> urlBeforeStyleEnd(String anchor) {
		int anchorIndex = html.indexOf(anchor);
		if (anchorIndex == -1) {
			return Optional.empty();
		}
		String prefix = html.substring(0, anchorIndex);
		int httpsIndex = prefix.lastIndexOf("https");
		int styleIndex = prefix.lastIndexOf("style");
		if (httpsIndex == -1 || styleIndex == -1 || httpsIndex >= styleIndex) {
			return Optional.empty();
		}
		return Optional.of(prefix.substring(httpsIndex, styleIndex - 2).trim());
	}

}
