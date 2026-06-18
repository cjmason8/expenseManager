package au.com.mason.expensemanager.processor;

import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.html.BillNoticeData;
import au.com.mason.expensemanager.html.rates.RatesFirstNoticeHtmlData;
import au.com.mason.expensemanager.html.rates.RatesInstalmentNoticeHtmlParser;
import au.com.mason.expensemanager.html.rates.WodongaRatesFirstNoticeHtmlParser;
import au.com.mason.expensemanager.mail.EmailMessageParts;
import au.com.mason.expensemanager.pdf.rates.RatesInstalmentData;
import au.com.mason.expensemanager.pdf.rates.WodongaRatesFirstNoticePdfParser;

@Component
public class WodongaRatesProcessor extends Processor {

	@Autowired
	private WodongaRatesFirstNoticePdfParser ratesFirstNoticePdfParser;

	@Autowired
	private RatesInstalmentNoticeHtmlParser ratesInstalmentNoticeHtmlParser;

	@Autowired
	private WodongaRatesFirstNoticeHtmlParser ratesFirstNoticeHtmlParser;

	@Override
	public void execute(Message message, RefData refData) throws Exception {
		if (message.getSubject().indexOf("Instalment") != -1) {
			handleInstalments(message, refData);
		} else {
			handleFirst(message, refData);
		}
	}

	private void handleInstalments(Message message, RefData refData) throws Exception {
		if (!EmailMessageParts.isMultipart(message)) {
			return;
		}

		BillNoticeData notice = null;
		byte[] pdfBytes = null;
		for (BodyPart bodyPart : EmailMessageParts.allParts(message)) {
			if (EmailMessageParts.isHtmlPart(bodyPart)) {
				notice = ratesInstalmentNoticeHtmlParser.parse(EmailMessageParts.readHtml(bodyPart));
			} else if (EmailMessageParts.isOctetStreamPart(bodyPart)) {
				pdfBytes = EmailMessageParts.readBytes(bodyPart);
			}
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
		String fileName = "WodongaRates-" + formatter.format(notice.dueDate()) + ".pdf";
		Document document = documentService.createDocumentFromEmailForExpense(pdfBytes, fileName);
		updateExpense(refData, notice.dueDate(), notice.amount(), document, null);
	}

	private void handleFirst(Message message, RefData refData) throws Exception {
		if (!EmailMessageParts.isMultipart(message)) {
			return;
		}

		byte[] pdfBytes = null;
		RatesFirstNoticeHtmlData firstNotice = null;
		for (BodyPart bodyPart : EmailMessageParts.allParts(message)) {
			if (EmailMessageParts.isHtmlPart(bodyPart)) {
				firstNotice = ratesFirstNoticeHtmlParser.parse(EmailMessageParts.readHtml(bodyPart));
			} else if (EmailMessageParts.isPdfPart(bodyPart)) {
				pdfBytes = EmailMessageParts.readBytes(bodyPart);
			}
		}

		List<RatesInstalmentData> instalments = ratesFirstNoticePdfParser.parse(
			pdfBytes, firstNotice.firstInstalmentAmount(), firstNotice.year());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
		String fileName = "WodongaRates-" + formatter.format(instalments.get(0).dueDate()) + ".pdf";
		Document document = documentService.createDocumentFromEmailForExpense(pdfBytes, fileName);

		for (RatesInstalmentData instalment : instalments) {
			Document instalmentDocument = instalment.instalmentNumber() == 1 ? document : null;
			createExpense(refData, instalment.dueDate(), instalmentDocument, instalment.notes(),
				instalment.amountAsBigDecimal());
		}
	}

}
