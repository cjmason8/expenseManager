package au.com.mason.expensemanager.processor;

import java.time.format.DateTimeFormatter;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.html.BillNoticeData;
import au.com.mason.expensemanager.html.lumo.LumoBillHtmlParser;
import au.com.mason.expensemanager.mail.EmailMessageParts;

@Component
public abstract class LumoProcessor extends Processor {

	@Autowired
	private LumoBillHtmlParser lumoBillHtmlParser;

	public void process(Message message, RefData refData, String prefix) throws Exception {
		if (EmailMessageParts.isPlainText(message) || !EmailMessageParts.isMultipart(message)) {
			return;
		}

		BillNoticeData bill = null;
		Document document = null;
		for (BodyPart bodyPart : EmailMessageParts.allParts(message)) {
			if (EmailMessageParts.isHtmlPart(bodyPart)) {
				bill = lumoBillHtmlParser.parse(EmailMessageParts.readHtml(bodyPart));
			} else if (bill != null) {
				byte[] pdfBytes = EmailMessageParts.readBytes(bodyPart);
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
				String fileName = prefix + formatter.format(bill.dueDate()) + ".pdf";
				document = documentService.createDocumentFromEmailForExpense(pdfBytes, fileName);
			}
		}

		updateExpense(refData, bill.dueDate(), bill.amount(), document);
	}

}
