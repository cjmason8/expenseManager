package au.com.mason.expensemanager.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;

import au.com.mason.expensemanager.domain.RefData;

class StockSoftwarePayslipProcessorTest {

	private static final LocalDate PAY_DATE = LocalDate.of(2026, 1, 15);

	@Test
	void buildDocumentMetadata_copiesRefDataMetadataAndOverlaysYear() {
		RefData refData = new RefData();
		refData.setMetaData(Map.of("type", "Payslip", "company", "Stock Software"));

		Map<String, Object> metaData = StockSoftwarePayslipProcessor.buildDocumentMetadata(refData, "2025-2026");

		assertEquals("Payslip", metaData.get("type"));
		assertEquals("Stock Software", metaData.get("company"));
		assertEquals("2025-2026", metaData.get("year"));
	}

	@Test
	void buildFileName_usesPayslipPrefixForStandardSubject() {
		assertEquals("Payslip - 15-01-2026.pdf",
			StockSoftwarePayslipProcessor.buildFileName("Payslip", null, PAY_DATE));
	}

	@Test
	void buildFileName_includesBonusSuffixFromSubject() {
		assertEquals("Payslip - Bonus - 15-01-2026.pdf",
			StockSoftwarePayslipProcessor.buildFileName("Payslip - Bonus", null, PAY_DATE));
	}

	@Test
	void buildFileName_includesMultiWordSuffixFromSubject() {
		assertEquals("Payslip - Xmas Bonus - 15-01-2026.pdf",
			StockSoftwarePayslipProcessor.buildFileName("Payslip - Xmas Bonus", null, PAY_DATE));
	}

	@Test
	void buildFileName_usesBonusFromAttachmentWhenSubjectIsPlainPayslip() {
		assertEquals("Payslip - Bonus - 15-01-2026.pdf",
			StockSoftwarePayslipProcessor.buildFileName("Payslip", "Payslip - Bonus.pdf", PAY_DATE));
	}

	@Test
	void buildFileName_stripsForwardedPrefixFromSubject() {
		assertEquals("Payslip - Bonus - 15-01-2026.pdf",
			StockSoftwarePayslipProcessor.buildFileName("Fwd: Payslip - Bonus", null, PAY_DATE));
	}

}
