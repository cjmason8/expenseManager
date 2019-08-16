package au.com.mason.expensemanager.processor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import com.sun.mail.util.BASE64DecoderStream;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.pdf.PdfReader;

@Component
public class WodongaRatesProccesor extends Processor {
	
	@Override
	public void execute(Message message, RefData refData) throws Exception {
		if (message.isMimeType("multipart/*")) {
	        MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
	        int count = mimeMultipart.getCount();
	        String firstInstalmentAmount = null;
		    for (int i = 0; i < count; i++) {
		        BodyPart bodyPart = mimeMultipart.getBodyPart(i);
		        if (bodyPart.isMimeType("text/html")) {
		        	String body = bodyPart.getContent().toString();
		        	System.out.println(body);
		        	body = body.substring(body.indexOf("Amount"));
		        	int dollarIndex = body.indexOf("$");
					firstInstalmentAmount = body.substring(dollarIndex + 1, body.indexOf("<", dollarIndex)); 
		        }
		        else if (bodyPart.getContentType().startsWith("APPLICATION/PDF")) {
		        	BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getContent();
		        	byte[] byteArray = IOUtils.toByteArray(base64DecoderStream);
					
					int step = 0;
					String content = PdfReader.extract(byteArray);
					System.out.println(content);
					final String reqParam = firstInstalmentAmount;
					String reqLine = Arrays.asList(content.split("\n")).stream().filter(line -> line.startsWith(reqParam)).findFirst().get();
					
					/*
					 * Instalment[] instalments = new Instalment[4]; instalments[0] = new
					 * Instalment(LocalDate.of(year, 9, 30)); instalments[1] = new
					 * Instalment(LocalDate.of(year, 11, 30)); instalments[2] = new
					 * Instalment(LocalDate.of(year, 2, 28)); instalments[3] = new
					 * Instalment(LocalDate.of(year, 5, 31));
					 * 
					 * instalments[0].setAmount(firstInstalment);
					 * 
					 * DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy"); String
					 * fileName = "DingleyRates-" + formatter.format(instalments[0].getDueDate()) +
					 * ".pdf"; Document document =
					 * documentService.createDocumentFromEmailForExpense(response.body(), fileName);
					 * instalments[0].setDocument(document);
					 * 
					 * Arrays.asList(content.split("\n")).stream().filter(line ->
					 * line.startsWith("$")).forEach(line -> { if (line.indexOf(firstInstalment) !=
					 * -1) { foundFirst[0] = true; } else if (foundFirst[0] && counter[0] <= 3) {
					 * instalments[counter[0]++].setAmount(line.replace("$", "")); } });
					 * Arrays.asList(instalments).stream().forEach(item -> { createExpense(refData,
					 * item.getDueDate(), item.getDocument(), item.getNotes(), item.getAmount());
					 * });
					 */
					
					
					//System.out.println(content);
					/*
					 * String[] lines = content.split("\n"); String[] instalments = null; String[]
					 * dates = null; String firstInstalment = null; for (String line : lines) { if
					 * (line.startsWith("Payments received after")) { step = 1; } else if (step ==
					 * 1) { instalments = line.split(" "); step = 2; } else if (step == 2) { dates =
					 * line.split(" "); step = 3; } else if (step == 3 &&
					 * line.indexOf("1st Instalment") != -1) { int startIndex = line.indexOf("$");
					 * firstInstalment = line.substring(startIndex, line.indexOf(" ", startIndex));
					 * } }
					 */
					
					/*
					 * DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
					 * DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
					 * String fileName = "WodongaRates-" +
					 * formatter.format(LocalDate.parse(dates[0], dateFormatter)) + ".pdf"; Document
					 * document = documentService.createDocumentFromEmailForExpense(byteArray,
					 * fileName);
					 * 
					 * updateExpense(refData, LocalDate.parse(dates[0], dateFormatter),
					 * firstInstalment, document); updateExpense(refData, LocalDate.parse(dates[1],
					 * dateFormatter), instalments[0], null); updateExpense(refData,
					 * LocalDate.parse(dates[2], dateFormatter), instalments[1], null);
					 * updateExpense(refData, LocalDate.parse(dates[3], dateFormatter),
					 * instalments[2], null);
					 */
		        }
		    }
	    }
	}

}
