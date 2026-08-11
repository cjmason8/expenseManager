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
import au.com.mason.expensemanager.html.rates.RatesInstalmentNoticeHtmlParser;
import au.com.mason.expensemanager.mail.EmailMessageParts;
import au.com.mason.expensemanager.pdf.rates.RatesInstalmentData;
import au.com.mason.expensemanager.pdf.rates.SouthKingsvilleRatesFirstNoticePdfParser;

@Component
public class SouthKingsvilleRatesProcessor extends Processor {

	@Autowired
	private SouthKingsvilleRatesFirstNoticePdfParser ratesFirstNoticePdfParser;

	@Autowired
	private RatesInstalmentNoticeHtmlParser ratesInstalmentNoticeHtmlParser;

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
			} else if (EmailMessageParts.isPdfAttachment(bodyPart)) {
				pdfBytes = EmailMessageParts.readBytes(bodyPart);
			}
		}

		if (notice == null || pdfBytes == null) {
			throw new IllegalStateException(
				"South Kingsville rates instalment email missing HTML notice or PDF attachment");
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
		String fileName = "SouthKingsvilleRates-" + formatter.format(notice.dueDate()) + ".pdf";
		Document document = documentService.createDocumentFromEmailForExpense(pdfBytes, fileName);
		updateExpense(refData, notice.dueDate(), notice.amount(), document, null);
	}

	private void handleFirst(Message message, RefData refData) throws Exception {
		if (!EmailMessageParts.isMultipart(message)) {
			return;
		}

		byte[] pdfBytes = null;
		for (BodyPart bodyPart : EmailMessageParts.allParts(message)) {
			if (!EmailMessageParts.isPdfAttachment(bodyPart)) {
				continue;
			}

			pdfBytes = EmailMessageParts.readBytes(bodyPart);
		}

		if (pdfBytes == null) {
			throw new IllegalStateException("South Kingsville rates first notice email missing PDF attachment");
		}

		List<RatesInstalmentData> instalments = ratesFirstNoticePdfParser.parse(pdfBytes);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
		String fileName = "SouthKingsvilleRates-" + formatter.format(instalments.get(0).dueDate()) + ".pdf";
		Document document = documentService.createDocumentFromEmailForExpense(pdfBytes, fileName);

		updateExpense(refData, instalments.get(0).dueDate(), instalments.get(0).amount(), document);
		for (int j = 1; j < instalments.size(); j++) {
			RatesInstalmentData instalment = instalments.get(j);
			updateExpense(refData, instalment.dueDate(), instalment.amount(), null);
		}
	}

}
