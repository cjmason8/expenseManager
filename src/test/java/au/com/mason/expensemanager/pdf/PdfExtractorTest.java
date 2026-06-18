package au.com.mason.expensemanager.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class PdfExtractorTest {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	@Test
	void amountFromLineStartingWith_extractsFirstDollarAmount() {
		PdfExtractor pdf = PdfExtractor.fromText("Money In $2,151.00\nMoney Out $146.91");

		assertEquals(new BigDecimal("2151.00"), pdf.amountFromLineStartingWith("Money In").orElseThrow());
	}

	@Test
	void amountFromLineContaining_extractsFirstDollarAmountOnMatchingLine() {
		PdfExtractor pdf = PdfExtractor.fromText("Invoice total $999.50\nOther line");

		assertEquals(new BigDecimal("999.50"), pdf.amountFromLineContaining("Invoice total").orElseThrow());
	}

	@Test
	void amountAfterAsteriskInLineContaining_extractsFeeAfterAsterisk() {
		PdfExtractor pdf = PdfExtractor.fromText("Management fee ... * $141.96");

		assertEquals(new BigDecimal("141.96"), pdf.amountAfterAsteriskInLineContaining("Management fee").orElseThrow());
	}

	@Test
	void amountAfterAsteriskInLineContaining_returnsEmptyWhenNoAsterisk() {
		PdfExtractor pdf = PdfExtractor.fromText("Management fee $141.96");

		assertTrue(pdf.amountAfterAsteriskInLineContaining("Management fee").isEmpty());
	}

	@Test
	void dateMatching_extractsDateFromPattern() {
		String line = "Rent paid to 10/12/2025 with part payment of $187.00 (previously paid to 10/11/2025 + $187.00)";
		PdfExtractor pdf = PdfExtractor.fromText(line);
		Pattern rentPaidTo = Pattern.compile("Rent paid to (\\d{2}/\\d{2}/\\d{4})");

		assertEquals(LocalDate.of(2025, 12, 10), pdf.dateMatching(line, rentPaidTo, DATE_FORMAT).orElseThrow());
	}

	@Test
	void firstDateInLineContaining_extractsFirstDateOnLine() {
		PdfExtractor pdf = PdfExtractor.fromText("Due 15/03/2026 final");

		assertEquals(LocalDate.of(2026, 3, 15), pdf.firstDateInLineContaining("Due", DATE_FORMAT).orElseThrow());
	}

	@Test
	void lineAfterContaining_returnsNextLine() {
		PdfExtractor pdf = PdfExtractor.fromText("Amount Due\n$123.45");

		assertTrue(pdf.lineAfterContaining("Amount Due").orElseThrow().contains("123.45"));
	}

	@Test
	void lineAfterContaining_returnsEmptyWhenNoFollowingLine() {
		PdfExtractor pdf = PdfExtractor.fromText("Amount Due");

		assertTrue(pdf.lineAfterContaining("Amount Due").isEmpty());
	}

	@Test
	void linesContaining_findsAllMatchingLines() {
		PdfExtractor pdf = PdfExtractor.fromText("$10.00\nignore\n$20.00");

		assertEquals(2, pdf.linesContaining("$").count());
	}

	@Test
	void fromBytes_extractsTextFromRealPdf() throws Exception {
		byte[] pdfBytes = Files.readAllBytes(Path.of("statement.pdf"));

		PdfExtractor pdf = PdfExtractor.fromBytes(pdfBytes);

		assertTrue(pdf.text().contains("Money In"));
		assertTrue(pdf.lineStartingWith("You Received").isPresent());
		assertFalse(pdf.lineContaining("Rent paid to").isEmpty());
	}

}
