package au.com.mason.expensemanager.pdf.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import au.com.mason.expensemanager.pdf.PdfTextExtractor;

@ExtendWith(MockitoExtension.class)
class GlobirdInvoicePdfParserTest {

	@Mock
	private PdfTextExtractor pdfTextExtractor;

	private GlobirdInvoicePdfParser parser;

	@BeforeEach
	void setUp() {
		parser = new GlobirdInvoicePdfParser(pdfTextExtractor);
	}

	@Test
	void parse_extractsStandardInvoice() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn(GlobirdInvoiceFixtures.STANDARD_INVOICE);

		GlobirdInvoiceData data = parser.parse(new byte[] { 1 });

		assertEquals(LocalDate.of(2026, 1, 1), data.issueDate());
		assertEquals(LocalDate.of(2026, 1, 15), data.dueDate());
		assertEquals("123.45", data.amount());
		assertTrue(!data.zeroCredit());
	}

	@Test
	void parse_detectsZeroCreditInvoiceWithoutDueDate() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn(GlobirdInvoiceFixtures.ZERO_CREDIT_INVOICE);

		GlobirdInvoiceData data = parser.parse(new byte[] { 1 });

		assertEquals(LocalDate.of(2026, 2, 1), data.issueDate());
		assertTrue(data.zeroCredit());
	}

}
