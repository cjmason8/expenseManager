package au.com.mason.expensemanager.processor;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.html.BillNoticeData;
import au.com.mason.expensemanager.html.dingley.DingleyWaterBillHtmlParser;
import au.com.mason.expensemanager.mail.EmailMessageParts;

@Component
public class DingleyWaterProcessor extends Processor {

	@Autowired
	private DingleyWaterBillHtmlParser dingleyWaterBillHtmlParser;

	@Override
	public void execute(Message message, RefData refData) throws Exception {
		if (!EmailMessageParts.isMultipart(message)) {
			return;
		}

		for (BodyPart bodyPart : EmailMessageParts.allParts(message)) {
			if (!EmailMessageParts.isHtmlPart(bodyPart)) {
				continue;
			}

			BillNoticeData bill = dingleyWaterBillHtmlParser.parse(EmailMessageParts.readHtml(bodyPart));
			updateExpense(refData, bill.dueDate(), bill.amount(), null, "PDF requires uploading");
		}
	}

}
