package au.com.mason.expensemanager.processor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.mail.Message;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.RefData;

@Component
public class DingleyWaterProccesor extends Processor {

	@Override
	public void execute(Message message, RefData refData) throws Exception {
		String body;
		if (message.isMimeType("text/html")) {
			body = message.getContent().toString();
			
			String amount = body.substring(body.indexOf("$") + 1, body.indexOf("<", body.indexOf("$"))).trim();
			
			int startIndex = body.indexOf("Date due") + 26;
			int beginIndex = body.indexOf("align=\"right\"", startIndex) + 14;
			String dueDateString = body.substring(beginIndex, body.indexOf("<", beginIndex));
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy").localizedBy(Locale.ENGLISH);
			LocalDate dueDate = LocalDate.parse(dueDateString, formatter);
			
			updateExpense(refData, dueDate, amount, null, "PDF requires uploading");
		}
	}

}
