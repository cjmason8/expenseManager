package au.com.mason.expensemanager.pdf.rental;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import au.com.mason.expensemanager.pdf.PdfTextExtractor;

@ExtendWith(MockitoExtension.class)
class WodongaRentalStatementPdfParserTest {

	@Mock
	private PdfTextExtractor pdfTextExtractor;

	private WodongaRentalStatementPdfParser parser;

	@BeforeEach
	void setUp() {
		parser = new WodongaRentalStatementPdfParser(pdfTextExtractor);
	}

	@Test
	void parse_extractsFieldsWhenAdminFeeIsOnFollowingLine() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn(WodongaStatementFixtures.STANDARD_STATEMENT);

		RentalStatementData data = parser.parse(new byte[] { 1 });

		assertEquals(new BigDecimal("1000.00"), data.totalRent());
		assertEquals(new BigDecimal("100.00"), data.managementFee());
		assertEquals(new BigDecimal("10.00"), data.adminFee());
		assertEquals(new BigDecimal("890.00"), data.paymentToOwner());
		assertEquals(LocalDate.of(2025, 7, 1), data.statementFrom());
		assertEquals(LocalDate.of(2025, 7, 31), data.statementTo());
		assertTrue(data.isBalanced());
	}

	@Test
	void parse_extractsFieldsWhenAdminFeeIsOnSameLine() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn(WodongaStatementFixtures.ADMIN_FEE_ON_SAME_LINE);

		RentalStatementData data = parser.parse(new byte[] { 1 });

		assertEquals(new BigDecimal("10.00"), data.adminFee());
		assertTrue(data.isBalanced());
	}

}
