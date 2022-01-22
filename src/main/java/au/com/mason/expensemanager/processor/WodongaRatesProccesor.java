package au.com.mason.expensemanager.processor;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

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
import au.com.mason.expensemanager.util.CollectionUtil;

@Component
public class WodongaRatesProccesor extends Processor {

	@Override
	public void execute(Message message, RefData refData) throws Exception {
		if (message.getSubject().indexOf("Instalment") != -1) {
			handleInstalments(message, refData);
		} else {
			handleFirst(message, refData);
		}
	}

	private void handleInstalments(Message message, RefData refData) throws Exception {
		if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			int count = mimeMultipart.getCount();
			LocalDate dueDate = null;
			String amount = null;
			Document document = null;
			byte[] byteArray = null;
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
				} else if (bodyPart.getContentType().startsWith("APPLICATION/OCTET-STREAM")) {
					BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getContent();
					byteArray = IOUtils.toByteArray(base64DecoderStream);
				}
			}
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
			String fileName = "WodongaRates-" + formatter.format(dueDate) + ".pdf";
			document = documentService.createDocumentFromEmailForExpense(byteArray, fileName);

			updateExpense(refData, dueDate, amount, document, null);
		}
	}

	private void handleFirst(Message message, RefData refData) throws MessagingException, IOException, Exception {
		if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			int count = mimeMultipart.getCount();
			String firstInstalmentAmount = null;
			int year = -1;
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = mimeMultipart.getBodyPart(i);
				if (bodyPart.isMimeType("text/html")) {
					String body = bodyPart.getContent().toString();
					body = body.substring(body.indexOf("Due"));
					int dollarIndex = body.indexOf("$");
					firstInstalmentAmount = body.substring(dollarIndex + 1, body.indexOf("<", dollarIndex));
					int yearIndex = body.indexOf(" 20") + 1;
					year = Integer.valueOf(body.substring(yearIndex, body.indexOf("<", yearIndex)));
				} else if (bodyPart.getContentType().startsWith("APPLICATION/PDF")) {
					BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getContent();
					byte[] byteArray = IOUtils.toByteArray(base64DecoderStream);

					String content = PdfReader.extract(byteArray);
					final String reqParam = firstInstalmentAmount;
					String reqLine = CollectionUtil.splitAndConvert(content, "\n").stream()
							.filter(line -> line.startsWith("$" + reqParam)).findFirst().get();
					String[] instalmentAmounts = reqLine.split(" ");

					Instalment[] instalments = new Instalment[4];
					instalments[0] = new Instalment(LocalDate.of(year, 9, 30));
					instalments[1] = new Instalment(LocalDate.of(year, 11, 30));
					instalments[2] = new Instalment(LocalDate.of(year + 1, 2, 28));
					instalments[3] = new Instalment(LocalDate.of(year + 1, 5, 31));

					instalments[0].setAmount(instalmentAmounts[0].substring(1));
					instalments[0].setNotes(1);
					instalments[1].setAmount(instalmentAmounts[1].substring(1));
					instalments[1].setNotes(2);
					instalments[2].setAmount(instalmentAmounts[2].substring(1));
					instalments[2].setNotes(3);
					instalments[3].setAmount(instalmentAmounts[3].substring(1));
					instalments[3].setNotes(4);

					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
					String fileName = "WodongaRates-" + formatter.format(instalments[0].getDueDate()) + ".pdf";
					Document document = documentService.createDocumentFromEmailForExpense(byteArray, fileName);
					instalments[0].setDocument(document);

					Arrays.asList(instalments).stream().forEach(item -> {
						createExpense(refData, item.getDueDate(), item.getDocument(), item.getNotes(),
								item.getAmount());
					});
				}
			}
		}
	}

}
