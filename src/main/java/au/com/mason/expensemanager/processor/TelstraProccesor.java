package au.com.mason.expensemanager.processor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import com.sun.mail.util.BASE64DecoderStream;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.RefData;

@Component
public class TelstraProccesor extends Processor {
	
	@Override
	public void execute(Message message, RefData refData) throws Exception {
		String body;
		if (message.isMimeType("multipart/*")) {
	        MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
	        int count = mimeMultipart.getCount();
	        Document document = null;
	        LocalDate dueDate = null;
	        String amount = null;
		    for (int i = 0; i < count; i++) {
		        BodyPart bodyPart = mimeMultipart.getBodyPart(i);
		        if (bodyPart.isMimeType("text/html")) {
		        	body = (String) bodyPart.getContent();
			        amount = body.substring(body.indexOf("&#36;") + 5, body.indexOf("&#160;", body.indexOf("&#36;"))).trim();
			        int startIndex = body.indexOf("Debit on") + 9;
		            String dueDateString = body.substring(startIndex, body.indexOf("<", startIndex)).trim();
		            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy").localizedBy(Locale.ENGLISH);
		            dueDate = LocalDate.parse(dueDateString, formatter);
		        } else if (bodyPart.getContentType().startsWith("APPLICATION/PDF")) {
		        	BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getContent();
		        	byte[] byteArray = IOUtils.toByteArray(base64DecoderStream);
		        	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
		        	String fileName = "Telstra-" + formatter.format(dueDate) + ".pdf";
					document = documentService.createDocumentFromEmailForExpense(byteArray, fileName);
		        }
		    }
		    
		    updateExpense(refData, dueDate, amount, document);
	    }
	}

}
