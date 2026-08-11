package au.com.mason.expensemanager.processor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.html.rates.RatesInstalmentNoticeHtmlParser;
import au.com.mason.expensemanager.pdf.rates.RatesInstalmentData;
import au.com.mason.expensemanager.pdf.rates.SouthKingsvilleRatesFirstNoticePdfParser;
import au.com.mason.expensemanager.service.DocumentService;
import au.com.mason.expensemanager.service.ExpenseService;
import au.com.mason.expensemanager.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class SouthKingsvilleRatesProcessorTest {

	private static final byte[] PDF_BYTES = new byte[] { 1, 2, 3 };

	private static final String INSTALMENT_HTML = """
		<html><body>
		<table>
		<tr><td>Due Date</td><td class="value">15 March 2026</td></tr>
		<tr><td>Amount Due</td><td class="value">$500.00</td></tr>
		</table>
		</body></html>
		""";

	@Mock
	private DocumentService documentService;

	@Mock
	private ExpenseService expenseService;

	@Mock
	private NotificationService notificationService;

	@Mock
	private SouthKingsvilleRatesFirstNoticePdfParser ratesFirstNoticePdfParser;

	private SouthKingsvilleRatesProcessor processor;

	@BeforeEach
	void setUp() {
		processor = new SouthKingsvilleRatesProcessor();
		ReflectionTestUtils.setField(processor, "ratesFirstNoticePdfParser", ratesFirstNoticePdfParser);
		ReflectionTestUtils.setField(processor, "ratesInstalmentNoticeHtmlParser", new RatesInstalmentNoticeHtmlParser());
		ReflectionTestUtils.setField(processor, "documentService", documentService);
		ReflectionTestUtils.setField(processor, "expenseService", expenseService);
		ReflectionTestUtils.setField(processor, "notificationService", notificationService);
	}

	@Test
	void execute_instalmentNotice_processesOctetStreamAttachmentBeforeHtml() throws Exception {
		MimeMessage message = instalmentMessage(INSTALMENT_HTML, PDF_BYTES, "APPLICATION/OCTET-STREAM", false);
		message.setSubject("Rates Instalment Notice");

		RefData refData = new RefData();
		refData.setDescription("South Kingsville Rates");
		Document document = new Document();
		Expense expense = new Expense();
		expense.setDueDate(LocalDate.of(2026, 3, 10));
		expense.setEntryType(refData);

		when(documentService.createDocumentFromEmailForExpense(PDF_BYTES, "SouthKingsvilleRates-15032026.pdf"))
			.thenReturn(document);
		when(expenseService.findExpense(refData)).thenReturn(List.of(expense));

		processor.execute(message, refData);

		verify(expenseService).update(any(Expense.class));
		verify(documentService).createDocumentFromEmailForExpense(PDF_BYTES, "SouthKingsvilleRates-15032026.pdf");
	}

	@Test
	void execute_instalmentNotice_throwsWhenPdfMissing() throws Exception {
		MimeMessage message = instalmentMessage(INSTALMENT_HTML, null, null, true);
		message.setSubject("Rates Instalment Notice");

		assertThrows(IllegalStateException.class, () -> processor.execute(message, new RefData()));
	}

	@Test
	void execute_firstNotice_processesPdfAttachment() throws Exception {
		MimeMessage message = instalmentMessage(null, PDF_BYTES, "APPLICATION/PDF", true);
		message.setSubject("Rates Notice");

		RefData refData = new RefData();
		Document document = new Document();
		List<RatesInstalmentData> instalments = List.of(
			new RatesInstalmentData(LocalDate.of(2025, 9, 1), "$150.00", 1),
			new RatesInstalmentData(LocalDate.of(2025, 11, 1), "$200.00", 2));

		when(ratesFirstNoticePdfParser.parse(PDF_BYTES)).thenReturn(instalments);
		when(documentService.createDocumentFromEmailForExpense(PDF_BYTES, "SouthKingsvilleRates-01092025.pdf"))
			.thenReturn(document);
		when(expenseService.findExpense(refData)).thenReturn(List.of());

		processor.execute(message, refData);

		verify(ratesFirstNoticePdfParser).parse(PDF_BYTES);
		verify(documentService).createDocumentFromEmailForExpense(PDF_BYTES, "SouthKingsvilleRates-01092025.pdf");
		verify(expenseService, times(2)).create(any(Expense.class));
	}

	private static MimeMessage instalmentMessage(String html, byte[] pdfBytes, String pdfContentType, boolean htmlFirst)
		throws Exception {
		jakarta.mail.Session session = jakarta.mail.Session.getDefaultInstance(new java.util.Properties());
		jakarta.mail.internet.MimeMessage message = new jakarta.mail.internet.MimeMessage(session);
		jakarta.mail.internet.MimeMultipart multipart = new jakarta.mail.internet.MimeMultipart();

		jakarta.mail.internet.MimeBodyPart htmlPart = null;
		if (html != null) {
			htmlPart = new jakarta.mail.internet.MimeBodyPart();
			htmlPart.setContent(html, "text/html");
		}

		jakarta.mail.internet.MimeBodyPart pdfPart = null;
		if (pdfBytes != null) {
			pdfPart = new jakarta.mail.internet.MimeBodyPart();
			pdfPart.setContent(pdfBytes, pdfContentType);
			pdfPart.setFileName("rates.pdf");
		}

		if (htmlFirst) {
			if (htmlPart != null) {
				multipart.addBodyPart(htmlPart);
			}
			if (pdfPart != null) {
				multipart.addBodyPart(pdfPart);
			}
		} else {
			if (pdfPart != null) {
				multipart.addBodyPart(pdfPart);
			}
			if (htmlPart != null) {
				multipart.addBodyPart(htmlPart);
			}
		}

		message.setContent(multipart);
		message.saveChanges();
		return message;
	}

}
