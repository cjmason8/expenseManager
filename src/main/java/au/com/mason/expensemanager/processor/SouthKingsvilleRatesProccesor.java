package au.com.mason.expensemanager.processor;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import com.sun.mail.util.BASE64DecoderStream;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.pdf.PdfReader;

@Component
public class SouthKingsvilleRatesProccesor extends Processor {
	
	@Override
	public void execute(Message message, RefData refData) throws Exception {
		if (message.getSubject().indexOf("Instalment") != -1) {
			handleInstalments(message, refData);
		}
		else {
			handleFirst(message, refData);
		}
	}
	
	private void handleInstalments(Message message, RefData refData) throws Exception {
		if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			int count = mimeMultipart.getCount();
			LocalDate dueDate = null;
			String amount = null;
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = mimeMultipart.getBodyPart(i);
				if (bodyPart.isMimeType("text/html")) {
					String body = bodyPart.getContent().toString();
					int startIndex = body.indexOf("\">", body.indexOf("Due Date") + 10) + 2;
					String dueDateString = body.substring(startIndex, body.indexOf("<", startIndex));
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
					dueDate = LocalDate.parse(dueDateString, formatter);
					startIndex = body.indexOf("\">", body.indexOf("Amount Due") + 12) + 2;
					amount = body.substring(startIndex, body.indexOf("<", startIndex));
				} else if (bodyPart.getContentType().startsWith("APPLICATION/PDF")) {
					BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getContent();
					byte[] byteArray = IOUtils.toByteArray(base64DecoderStream);

					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
					String fileName = "SouthKingsvilleRates-" + formatter.format(dueDate) + ".pdf";
					 Document document =
					 documentService.createDocumentFromEmailForExpense(byteArray, fileName);

					 updateExpense(refData, dueDate, amount, document, null);
				}
			}
		}		
	}

	private void handleFirst(Message message, RefData refData) throws MessagingException, IOException, Exception {
		if (message.isMimeType("multipart/*")) {
	        MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
	        int count = mimeMultipart.getCount();
		    for (int i = 0; i < count; i++) {
		        BodyPart bodyPart = mimeMultipart.getBodyPart(i);
		        if (bodyPart.isMimeType("text/html")) {
		        } else if (bodyPart.getContentType().startsWith("APPLICATION/PDF")) {
		        	BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getContent();
		        	byte[] byteArray = IOUtils.toByteArray(base64DecoderStream);
					
					int step = 0;
					String content = PdfReader.extract(byteArray);
					String[] lines = content.split("\n");
					String[] instalments = null;
					String[] dates = null;
					String firstInstalment = null;
					for (String line : lines) {
						if (line.startsWith("Payments received after")) {
							step = 1;
						}
						else if (step == 1) {
							instalments = line.split(" ");
							step = 2;
						}
						else if (step == 2) {
							dates = line.split(" ");
							step = 3;
						}
						else if (step == 3 && line.indexOf("1st Instalment") != -1) {
							int startIndex = line.indexOf("$");
							firstInstalment = line.substring(startIndex, line.indexOf(" ", startIndex));
						}
					}
					
		        	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
		        	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		        	String fileName = "SouthKingsvilleRates-" + formatter.format(LocalDate.parse(dates[0], dateFormatter)) + ".pdf";
					Document document = documentService.createDocumentFromEmailForExpense(byteArray, fileName);
					
					updateExpense(refData, LocalDate.parse(dates[0], dateFormatter), firstInstalment, document);
					updateExpense(refData, LocalDate.parse(dates[1], dateFormatter), instalments[0], null);
					updateExpense(refData, LocalDate.parse(dates[2], dateFormatter), instalments[1], null);
					updateExpense(refData, LocalDate.parse(dates[3], dateFormatter), instalments[2], null);
		        }
		    }
	    }
	}

}
