package au.com.mason.expensemanager.processor;

import java.io.BufferedInputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.RefData;

@Component
public class SouthKingsvilleWaterProccesor extends Processor {

	@Override
	public void execute(Message message, RefData refData) throws Exception {
		String body;
		System.out.println("Entering South Kingsville Water");
		if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			int count = mimeMultipart.getCount();
			LocalDate dueDate = null;
			String amount = null;
			Document document = null;
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = mimeMultipart.getBodyPart(i);
				if (bodyPart.isMimeType("text/html")) {
					body = (String) bodyPart.getContent();
					amount = body.substring(body.indexOf(">$") + 2, body.indexOf("<", body.indexOf(">$"))).trim();
					int startIndex = body.indexOf(">By") + 4;
		            String dueDateString = body.substring(startIndex, body.indexOf("<", startIndex)).trim().toLowerCase();
		            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy").localizedBy(Locale.ENGLISH);
		            String newDueDateString = dueDateString.substring(0, dueDateString.indexOf(" "))
		            		+ " " + dueDateString.substring(dueDateString.indexOf(" ") + 1, dueDateString.indexOf(" ") + 2).toUpperCase()
		            		+ dueDateString.substring(dueDateString.indexOf(" ") + 2); 
		            dueDate = LocalDate.parse(newDueDateString, formatter);
		            
		            startIndex = body.indexOf("<a href=\"https://view") + 9;
		            String url = body.substring(startIndex, body.indexOf("\"", startIndex));

					BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
					
		        	DateTimeFormatter fileDateFormatter = DateTimeFormatter.ofPattern("ddMMyyyy");
		        	String fileName = "SouthKingsvilleWater-" + fileDateFormatter.format(dueDate) + ".pdf";
					document = documentService.createDocumentFromEmail(in.readAllBytes(), fileName);
					
					updateExpense(refData, dueDate, amount, document);
				}
			}
		}
	}

}
