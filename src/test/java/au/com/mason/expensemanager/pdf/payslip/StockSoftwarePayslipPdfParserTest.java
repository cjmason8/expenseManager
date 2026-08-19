package au.com.mason.expensemanager.pdf.payslip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import au.com.mason.expensemanager.pdf.PdfTextExtractor;

@ExtendWith(MockitoExtension.class)
class StockSoftwarePayslipPdfParserTest {

	@Mock
	private PdfTextExtractor pdfTextExtractor;

	private StockSoftwarePayslipPdfParser parser;

	@BeforeEach
	void setUp() {
		parser = new StockSoftwarePayslipPdfParser(pdfTextExtractor);
	}

	@Test
	void parse_extractsPayToDateFromSlashFormat() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn("""
			Stock Software Pty Ltd
			Payslip
			Pay To Date: 15/01/2026
			Net Pay $4,500.00
			""");

		PayslipData data = parser.parse(new byte[] { 1 });

		assertEquals(LocalDate.of(2026, 1, 15), data.payToDate());
		assertNull(data.annualLeaveFullTimeYtdHours());
	}

	@Test
	void parse_extractsPaymentDateWhenPayToDateMissing() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn("""
			Stock Software Pty Ltd
			Payslip
			Payment Date 31-07-2025
			Net Pay $4,500.00
			""");

		PayslipData data = parser.parse(new byte[] { 1 });

		assertEquals(LocalDate.of(2025, 7, 31), data.payToDate());
		assertNull(data.annualLeaveFullTimeYtdHours());
	}

	@Test
	void parse_extractsAnnualLeaveFullTimeYtdFromLeaveTable() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn("""
			Stock Software Pty Ltd
			Payslip
			Pay To Date: 15/01/2026
			Leave Type    Accrual    Taken    Current    YTD
			Annual Leave - FullTime    1.54    0.00    120.00    128.00
			Net Pay $4,500.00
			""");

		PayslipData data = parser.parse(new byte[] { 1 });

		assertEquals(new BigDecimal("128.00"), data.annualLeaveFullTimeYtdHours());
	}

	@Test
	void extractAnnualLeaveFullTimeYtd_usesLastNumberWhenHeaderMissing() {
		List<String> lines = List.of(
			"Annual Leave - FullTime 1.54 0.00 120.00 128.00",
			"Pay To Date: 15/01/2026");

		assertEquals(new BigDecimal("128.00"),
			StockSoftwarePayslipPdfParser.extractAnnualLeaveFullTimeYtd(lines).orElseThrow());
	}

}
