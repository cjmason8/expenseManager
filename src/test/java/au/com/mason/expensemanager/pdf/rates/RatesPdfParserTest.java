package au.com.mason.expensemanager.pdf.rates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import au.com.mason.expensemanager.pdf.PdfTextExtractor;

@ExtendWith(MockitoExtension.class)
class RatesPdfParserTest {

	@Mock
	private PdfTextExtractor pdfTextExtractor;

	private SouthKingsvilleRatesFirstNoticePdfParser southKingsvilleParser;
	private WodongaRatesFirstNoticePdfParser wodongaParser;
	private DingleyRatesInstalmentNoticePdfParser dingleyInstalmentParser;
	private DingleyRatesFirstNoticePdfParser dingleyFirstParser;

	@BeforeEach
	void setUp() {
		southKingsvilleParser = new SouthKingsvilleRatesFirstNoticePdfParser(pdfTextExtractor);
		wodongaParser = new WodongaRatesFirstNoticePdfParser(pdfTextExtractor);
		dingleyInstalmentParser = new DingleyRatesInstalmentNoticePdfParser(pdfTextExtractor);
		dingleyFirstParser = new DingleyRatesFirstNoticePdfParser(pdfTextExtractor);
	}

	@Test
	void southKingsvilleParser_extractsFourInstalments() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn(RatesNoticeFixtures.SOUTH_KINGSVILLE_FIRST_NOTICE);

		List<RatesInstalmentData> instalments = southKingsvilleParser.parse(new byte[] { 1 });

		assertEquals(4, instalments.size());
		assertEquals(LocalDate.of(2025, 9, 1), instalments.get(0).dueDate());
		assertEquals("$150.00", instalments.get(0).amount());
		assertEquals("$200.00", instalments.get(1).amount());
		assertEquals(LocalDate.of(2026, 5, 1), instalments.get(3).dueDate());
		assertEquals("1st Instalment", instalments.get(0).notes());
	}

	@Test
	void wodongaParser_extractsFourInstalmentsFromAmountLine() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn(RatesNoticeFixtures.WODONGA_FIRST_NOTICE);

		List<RatesInstalmentData> instalments = wodongaParser.parse(new byte[] { 1 }, "150.00", 2025);

		assertEquals(4, instalments.size());
		assertEquals(LocalDate.of(2025, 9, 30), instalments.get(0).dueDate());
		assertEquals("150.00", instalments.get(0).amount());
		assertEquals("300.00", instalments.get(3).amount());
	}

	@Test
	void dingleyInstalmentParser_extractsDueDateAndAmount() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn(RatesNoticeFixtures.DINGLEY_INSTALMENT_NOTICE);

		RatesInstalmentNoticeData notice = dingleyInstalmentParser.parse(new byte[] { 1 });

		assertEquals(LocalDate.of(2026, 3, 15), notice.dueDate());
		assertEquals("500.00", notice.amount());
	}

	@Test
	void dingleyFirstParser_extractsFourInstalments() throws Exception {
		when(pdfTextExtractor.extractText(any())).thenReturn(RatesNoticeFixtures.DINGLEY_FIRST_NOTICE);

		List<RatesInstalmentData> instalments = dingleyFirstParser.parse(new byte[] { 1 }, "150.00", 2025);

		assertEquals(4, instalments.size());
		assertEquals("150.00", instalments.get(0).amount());
		assertEquals("200.00", instalments.get(1).amount());
		assertEquals(LocalDate.of(2026, 5, 31), instalments.get(3).dueDate());
	}

}
