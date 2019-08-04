package au.com.mason.expensemanager.processor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import com.sun.mail.util.BASE64DecoderStream;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.RefData;

@Component
public class WodongaWaterProccesor extends Processor {
	
	@Override
	public void execute(Message message, RefData refData) throws Exception {
		String body;
		if (message.isMimeType("text/plain")) {
	        body = message.getContent().toString();
	    } else if (message.isMimeType("multipart/*")) {
	        MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
	        int count = mimeMultipart.getCount();
	        LocalDate dueDate = null;
	        String amount = null;
	        Document document = null;
		    for (int i = 0; i < count; i++) {
		        BodyPart bodyPart = mimeMultipart.getBodyPart(i);
		        if (bodyPart.isMimeType("text/html")) {
		        	body = (String) bodyPart.getContent();
		            int beginIndex = body.indexOf("zwnj", body.indexOf("Amount Due")) + 6;
					amount = body.substring(beginIndex, body.indexOf("<", beginIndex));
					beginIndex = body.indexOf("zwnj", body.indexOf("Due Date")) + 5;
		            String dueDateString = body.substring(beginIndex, body.indexOf("<", beginIndex));
		            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
		            dueDate = LocalDate.parse(dueDateString, formatter);
		            
		        } else if (bodyPart.getContentType().startsWith("APPLICATION/PDF")) {
		        	BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getContent();
		        	byte[] byteArray = IOUtils.toByteArray(base64DecoderStream);
		        	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
		        	String fileName = "WodongaWater-" + formatter.format(dueDate) + ".pdf";
					document = documentService.createDocumentFromEmailForExpense(byteArray, fileName);
		        }
		    }
		    
		    updateExpense(refData, dueDate, amount, document);
	    }
	}

}
