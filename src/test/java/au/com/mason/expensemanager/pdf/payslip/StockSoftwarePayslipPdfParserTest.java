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
		assertNull(data.annualLeaveYtdHours());
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
		assertNull(data.annualLeaveYtdHours());
	}

	@Test
	void parse_extractsAnnualLeaveYtdFromLeaveTable() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn("""
			Stock Software Pty Ltd
			Payslip
			Pay To Date: 15/01/2026
			Leave Type    Accrual    Taken    Current    YTD
			Annual Leave - FullTime    1.54    0.00    120.00    128.00
			Net Pay $4,500.00
			""");

		PayslipData data = parser.parse(new byte[] { 1 });

		assertEquals(new BigDecimal("128.00"), data.annualLeaveYtdHours());
	}

	@Test
	void parse_sumsBothAnnualLeaveArrangementRows() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn("""
			Stock Software Pty Ltd
			Payslip
			Pay To Date: 15/01/2026
			Leave Type    Accrual    Taken    Current    YTD
			Annual Leave - 9/10 Time    1.39    0.00    40.00    44.50
			Annual Leave - FullTime    1.54    0.00    120.00    128.00
			Net Pay $4,500.00
			""");

		PayslipData data = parser.parse(new byte[] { 1 });

		assertEquals(new BigDecimal("172.50"), data.annualLeaveYtdHours());
	}

	@Test
	void extractAnnualLeaveYtd_sumsArrangementRowsWhenHeaderMissing() {
		List<String> lines = List.of("Annual Leave - 9/10 Time 1.39 0.00 40.00 44.50",
			"Annual Leave - FullTime 1.54 0.00 120.00 128.00", "Pay To Date: 15/01/2026");

		assertEquals(new BigDecimal("172.50"),
			StockSoftwarePayslipPdfParser.extractAnnualLeaveYtd(lines).orElseThrow());
	}

	@Test
	void extractAnnualLeaveYtd_ignoresDigitsInArrangementLabel() {
		List<String> lines = List.of("Leave Type Accrual Taken Current YTD",
			"Annual Leave - 9/10 Time 1.39 0.00 40.00 44.50");

		assertEquals(new BigDecimal("44.50"), StockSoftwarePayslipPdfParser.extractAnnualLeaveYtd(lines).orElseThrow());
	}

	/**
	 * Real payslip layout: columns are sparse, so an arrangement that is no longer
	 * accruing shows a YTD figure only, and a trailing non-numeric TYPE column
	 * follows YTD.
	 */
	@Test
	void extractAnnualLeaveYtd_sumsSparseEntitlementRowsFromRealPayslip() {
		List<String> lines = List.of("DESCRIPTION HOURS CALC. RATE AMOUNT YTD TYPE",
			"Base Salary $5,662.94 $36,887.73 Wages", "PAYG Withholding -$1,220.00 -$8,670.00 Tax",
			"Annual Leave - 9/10 Time 5.77 5.77 Entitlements", "Annual Leave - FullTime 84.32 Entitlements",
			"SG $679.55 $4,454.85 Superannuation Expenses");

		assertEquals(new BigDecimal("90.09"),
			StockSoftwarePayslipPdfParser.extractAnnualLeaveYtd(lines).orElseThrow());
	}

	@Test
	void extractAnnualLeaveYtd_readsValuesFromLineBelowLabelWithoutDoubleCounting() {
		List<String> lines = List.of("Annual Leave - 9/10 Time", "1.39 0.00 40.00 44.50", "Annual Leave - FullTime",
			"1.54 0.00 120.00 128.00");

		assertEquals(new BigDecimal("172.50"),
			StockSoftwarePayslipPdfParser.extractAnnualLeaveYtd(lines).orElseThrow());
	}

	@Test
	void extractAnnualLeaveYtd_usesLastNumberWhenHeaderMissing() {
		List<String> lines = List.of("Annual Leave - FullTime 1.54 0.00 120.00 128.00", "Pay To Date: 15/01/2026");

		assertEquals(new BigDecimal("128.00"),
			StockSoftwarePayslipPdfParser.extractAnnualLeaveYtd(lines).orElseThrow());
	}

}
