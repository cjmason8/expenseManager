package au.com.mason.expensemanager.processor;

import java.time.format.DateTimeFormatter;

import jakarta.mail.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.html.BillNoticeData;
import au.com.mason.expensemanager.html.racv.RacvBillHtmlParser;
import au.com.mason.expensemanager.mail.EmailMessageParts;

@Component
public abstract class RACVProcessor extends Processor {

	@Autowired
	private RacvBillHtmlParser racvBillHtmlParser;

	protected void process(Message message, RefData refData, String pdfName) throws Exception {
		if (EmailMessageParts.isPlainText(message) || !EmailMessageParts.isMultipart(message)) {
			return;
		}

		BillNoticeData bill = racvBillHtmlParser.parse(EmailMessageParts.firstHtml(message).orElseThrow());
		byte[] pdfBytes = EmailMessageParts.lastNonHtmlAttachment(message).orElseThrow();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
		String fileName = pdfName + formatter.format(bill.dueDate()) + ".pdf";
		Document document = documentService.createDocumentFromEmailForExpense(pdfBytes, fileName);

		updateExpense(refData, bill.dueDate(), bill.amount(), document);
	}

}
