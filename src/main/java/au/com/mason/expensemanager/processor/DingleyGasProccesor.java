package au.com.mason.expensemanager.processor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
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
public class DingleyGasProccesor extends Processor {
	
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
		        if (bodyPart.isMimeType("multipart/*")) {
		        	MimeMultipart mimeMultipart2 = (MimeMultipart) bodyPart.getContent();
		        	for (int j = 0; j < mimeMultipart2.getCount(); j++) {
		        		BodyPart bodyPart2 = mimeMultipart2.getBodyPart(j);
		        		if (bodyPart2.isMimeType("text/html")) {
			        		body = (String) bodyPart2.getContent();
				            int startIndex = body.indexOf("$") + 1;
							int stopIndex = body.indexOf("<", startIndex);
							amount = body.substring(startIndex, stopIndex).trim();
							startIndex = body.indexOf("Direct debit") + 20;
				            String dueDateString = body.substring(startIndex, body.indexOf("<", startIndex)).trim();
				            DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d MMM yy").toFormatter().localizedBy(Locale.ENGLISH);
	
				            dueDate = LocalDate.parse(dueDateString, formatter);		        		
		        		}
		        	}
		        } else {
		        	BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getContent();
		        	byte[] byteArray = IOUtils.toByteArray(base64DecoderStream);
		        	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
		        	String fileName = "DingleyGas-" + formatter.format(dueDate) + ".pdf";
					document = documentService.createDocumentFromEmailForExpense(byteArray, fileName);
		        }
		    }
		    
            updateExpense(refData, dueDate, amount, document);
	    }
	}		
}
