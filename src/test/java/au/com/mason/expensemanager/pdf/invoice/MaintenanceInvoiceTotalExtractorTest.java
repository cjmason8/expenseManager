package au.com.mason.expensemanager.pdf.invoice;

import static au.com.mason.expensemanager.pdf.invoice.MaintenanceInvoiceTotalExtractor.UNKNOWN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import au.com.mason.expensemanager.pdf.PdfTextExtractor;

@ExtendWith(MockitoExtension.class)
class MaintenanceInvoiceTotalExtractorTest {

	private static final byte[] PDF_BYTES = "%PDF-1.4 stub".getBytes(StandardCharsets.ISO_8859_1);

	@Mock
	private PdfTextExtractor pdfTextExtractor;

	private MaintenanceInvoiceTotalExtractor extractor;

	@BeforeEach
	void setUp() {
		extractor = new MaintenanceInvoiceTotalExtractor(pdfTextExtractor);
	}

	static Stream<Arguments> invoices() {
		return Stream.of(Arguments.of("xero total aud", MaintenanceInvoiceFixtures.XERO_TOTAL_AUD, "176.00"),
			Arguments.of("total ahead of line items", MaintenanceInvoiceFixtures.TRADIFY_TOTAL_FIRST, "735.50"),
			Arguments.of("grand total with balance due", MaintenanceInvoiceFixtures.SERVICE_GRAND_TOTAL, "1540.00"),
			Arguments.of("total inc gst", MaintenanceInvoiceFixtures.TOTAL_INC_GST, "99.00"),
			Arguments.of("myob total amount", MaintenanceInvoiceFixtures.MYOB_TOTAL_AMOUNT, "110.00"),
			Arguments.of("amount before label", MaintenanceInvoiceFixtures.AMOUNT_BEFORE_LABEL, "15.00"),
			Arguments.of("total split across lines", MaintenanceInvoiceFixtures.TOTAL_SPLIT_ACROSS_LINES, "814.00"),
			Arguments.of("dollar sign with space", MaintenanceInvoiceFixtures.DOLLAR_WITH_SPACE, "110.00"),
			Arguments.of("quickbooks balance due", MaintenanceInvoiceFixtures.QUICKBOOKS_BALANCE_DUE, "345.00"),
			Arguments.of("only amount due", MaintenanceInvoiceFixtures.ONLY_AMOUNT_DUE, "324.50"),
			Arguments.of("prose mentioning total", MaintenanceInvoiceFixtures.PROSE_MENTIONS_TOTAL, "374.00"),
			Arguments.of("deposit paid", MaintenanceInvoiceFixtures.DEPOSIT_PAID, "3300.00"),
			Arguments.of("fully paid", MaintenanceInvoiceFixtures.FULLY_PAID, "379.00"),
			Arguments.of("letter spaced total", MaintenanceInvoiceFixtures.LETTER_SPACED_TOTAL, "385.00"),
			Arguments.of("shared fence", MaintenanceInvoiceFixtures.SHARED_FENCE, UNKNOWN),
			Arguments.of("quote without total", MaintenanceInvoiceFixtures.QUOTE_WITHOUT_TOTAL, UNKNOWN),
			Arguments.of("blank total on form", MaintenanceInvoiceFixtures.BLANK_TOTAL_FORM, UNKNOWN),
			Arguments.of("scanned pdf without text", "  \n \n", UNKNOWN));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("invoices")
	void extractTotal_findsInvoiceTotal(String name, String text, String expected) throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn(text);

		assertEquals(expected, extractor.extractTotal(PDF_BYTES));
	}

	@Test
	void extractTotal_fallsBackToPositionSortedText() throws Exception {
		when(pdfTextExtractor.extractText(any()))
			.thenReturn(MaintenanceInvoiceFixtures.LABELS_AND_VALUES_IN_COLUMNS);
		when(pdfTextExtractor.extractTextSortedByPosition(any()))
			.thenReturn(MaintenanceInvoiceFixtures.LABELS_AND_VALUES_SORTED_BY_POSITION);

		assertEquals("568.00", extractor.extractTotal(PDF_BYTES));
	}

	@Test
	void extractTotal_skipsPositionSortedTextWhenTotalFound() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn(MaintenanceInvoiceFixtures.XERO_TOTAL_AUD);

		assertEquals("176.00", extractor.extractTotal(PDF_BYTES));
		verify(pdfTextExtractor, never()).extractTextSortedByPosition(any());
	}

	@Test
	void extractTotal_isUnknownForNonPdf() {
		assertEquals(UNKNOWN, extractor.extractTotal(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}));
		verifyNoInteractions(pdfTextExtractor);
	}

	@Test
	void extractTotal_isUnknownWhenPdfCannotBeRead() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenThrow(new IOException("corrupt"));

		assertEquals(UNKNOWN, extractor.extractTotal(PDF_BYTES));
	}

	@Test
	void isPdf_detectsPdfHeader() {
		assertTrue(MaintenanceInvoiceTotalExtractor.isPdf(PDF_BYTES));
		assertFalse(MaintenanceInvoiceTotalExtractor.isPdf(null));
		assertFalse(MaintenanceInvoiceTotalExtractor.isPdf("hello world".getBytes(StandardCharsets.UTF_8)));
	}

}
