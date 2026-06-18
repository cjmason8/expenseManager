package au.com.mason.expensemanager.pdf.rental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import au.com.mason.expensemanager.pdf.PdfTextExtractor;

@ExtendWith(MockitoExtension.class)
class SouthKingsvilleRentalStatementPdfParserTest {

	@Mock
	private PdfTextExtractor pdfTextExtractor;

	private SouthKingsvilleRentalStatementPdfParser parser;

	@BeforeEach
	void setUp() {
		parser = new SouthKingsvilleRentalStatementPdfParser(pdfTextExtractor);
	}

	@Test
	void parse_extractsFieldsFromSampleStatementText() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn(SouthKingsvilleStatementFixtures.STANDARD_STATEMENT);

		RentalStatementData data = parser.parse(new byte[] { 1 });

		assertEquals(new BigDecimal("2151.00"), data.totalRent());
		assertEquals(new BigDecimal("141.96"), data.managementFee());
		assertEquals(new BigDecimal("4.95"), data.adminFee());
		assertEquals(new BigDecimal("2004.09"), data.paymentToOwner());
		assertEquals(LocalDate.of(2025, 11, 10), data.statementFrom());
		assertEquals(LocalDate.of(2025, 12, 10), data.statementTo());
		assertTrue(data.isBalanced());
	}

	@Test
	void parse_extractsFieldsFromRealStatementPdf() throws Exception {
		parser = new SouthKingsvilleRentalStatementPdfParser(new PdfTextExtractor());
		byte[] pdfBytes = Files.readAllBytes(Path.of("statement.pdf"));

		RentalStatementData data = parser.parse(pdfBytes);

		assertEquals(new BigDecimal("2151.00"), data.totalRent());
		assertEquals(new BigDecimal("141.96"), data.managementFee());
		assertEquals(new BigDecimal("4.95"), data.adminFee());
		assertEquals(new BigDecimal("2004.09"), data.paymentToOwner());
		assertEquals(LocalDate.of(2025, 11, 10), data.statementFrom());
		assertEquals(LocalDate.of(2025, 12, 10), data.statementTo());
		assertTrue(data.isBalanced());
	}

	@Test
	void parse_usesMovedInDateWhenTenantMovedIn() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn(SouthKingsvilleStatementFixtures.MOVED_IN_STATEMENT);

		RentalStatementData data = parser.parse(new byte[] { 1 });

		assertEquals(LocalDate.of(2026, 6, 10), data.statementFrom());
		assertEquals(LocalDate.of(2026, 7, 10), data.statementTo());
		assertTrue(data.isBalanced());
	}

	@Test
	void parse_detectsUnbalancedStatement() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn(SouthKingsvilleStatementFixtures.UNBALANCED_STATEMENT);

		RentalStatementData data = parser.parse(new byte[] { 1 });

		assertFalse(data.isBalanced());
	}

	@Test
	void parse_throwsWhenMoneyInMissing() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn("You Received $1.00");

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> parser.parse(new byte[] { 1 }));

		assertTrue(error.getMessage().contains("Money In"));
	}

	@Test
	void parse_throwsWhenRentPeriodMissing() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn("""
			Money In $100.00
			You Received $90.00
			Management fee ... * $5.00
			Accounting Fee * $5.00
			""");

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> parser.parse(new byte[] { 1 }));

		assertTrue(error.getMessage().contains("Rent paid to"));
	}

}
