package au.com.mason.expensemanager.processor;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.pdf.PdfReader;
import au.com.mason.expensemanager.util.CollectionUtil;

@Component
public class DingleyRatesProcessor extends Processor {

	@Override
	public void execute(Message message, RefData refData) throws Exception {
		if (message.getSubject().indexOf("Instalment") != -1) {
			handleInstalments(message, refData);
		}
		else {
			handleFirst(message, refData);
		}
	}
	
	private void handleInstalments(Message message, RefData refData)
			throws MessagingException, IOException, InterruptedException, Exception {
		if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			int count = mimeMultipart.getCount();
			
			if (mimeMultipart.getBodyPart(0).isMimeType("multipart/*")) {
				MimeMultipart innerMimeMultipart = (MimeMultipart) mimeMultipart.getBodyPart(0).getContent();
				for (int j = 0; j < innerMimeMultipart.getCount(); j++) {
					BodyPart innerBodyPart = innerMimeMultipart.getBodyPart(j);
					
					processTextHTML(refData, innerBodyPart);
				}
			}
			else {
				for (int i = 0; i < count; i++) {
					BodyPart bodyPart = mimeMultipart.getBodyPart(i);
					processTextHTML(refData, bodyPart);
				}
			}
		}
	}

	private void processTextHTML(RefData refData, BodyPart bodyPart)
			throws MessagingException, IOException, InterruptedException, Exception {
		if (bodyPart.getContentType().startsWith("TEXT/HTML")) {
			String body = bodyPart.getContent().toString();
			String findUrl = body.substring(0, body.indexOf("Click here to follow link"));
			int lastIndexOfHttp = findUrl.lastIndexOf("http");
			String url = findUrl.substring(lastIndexOfHttp, findUrl.indexOf("\"", lastIndexOfHttp));
			int year = 2018;
			while (body.indexOf("&#47;" + year + "</span>") == -1) {
				year++;
			}

			HttpClient client = HttpClient.newBuilder().version(Version.HTTP_2).followRedirects(Redirect.ALWAYS)
					.build();

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofMinutes(1))
					.GET().build();

			HttpResponse<byte[]> response = client.send(request, BodyHandlers.ofByteArray());
			
			String content = PdfReader.extract(response.body());

			LocalDate[] dueDate = new LocalDate[1];
			String[] amount = new String[1];
			
			DateTimeFormatter dueDateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
			
			CollectionUtil.splitAndConvert(content, "\n").stream().forEach(line -> {
				if (line.indexOf("Total Amount Due") != -1) {
					line = line.replace("Total Amount Due ", "");
					dueDate[0] = LocalDate.parse(line.substring(0, line.indexOf("$") -1), dueDateFormatter);
					amount[0] = line.substring(line.indexOf("$") + 1);
				}
			});
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
			
			String fileName = "DingleyRates-" + formatter.format(dueDate[0]) + ".pdf";
			Document document = documentService.createDocumentFromEmailForExpense(response.body(), fileName);

			updateExpense(refData, dueDate[0], amount[0], document, null);
		}

	}

	private void handleFirst(Message message, RefData refData)
			throws MessagingException, IOException, InterruptedException, Exception {
		if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			int count = mimeMultipart.getCount();
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = mimeMultipart.getBodyPart(i);
				if (bodyPart.getContentType().startsWith("TEXT/HTML")) {
					String body = bodyPart.getContent().toString();
					String findUrl = body.substring(0, body.indexOf("Click here to follow link"));
					int lastIndexOfHttp = findUrl.lastIndexOf("http");
					String url = findUrl.substring(lastIndexOfHttp, findUrl.indexOf("\"", lastIndexOfHttp));
					int year = 2018;
					while (body.indexOf("&#47;" + year + "</span>") == -1) {
						year++;
					}

					int startIndex = body.indexOf("&#36;") + 5;
					String firstInstalment = body.substring(startIndex, body.indexOf("<", startIndex));

					HttpClient client = HttpClient.newBuilder().version(Version.HTTP_2).followRedirects(Redirect.ALWAYS)
							.build();

					HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofMinutes(1))
							.GET().build();

					HttpResponse<byte[]> response = client.send(request, BodyHandlers.ofByteArray());
					
					String content = PdfReader.extract(response.body());
					Boolean[] foundFirst = new Boolean[1];
					foundFirst[0] = false;
					int[] counter = new int[1];
					counter[0] = 1;
					Instalment[] instalments = new Instalment[4];
					instalments[0] = new Instalment(LocalDate.of(year, 9, 30));
					instalments[1] = new Instalment(LocalDate.of(year, 11, 30));
					instalments[2] = new Instalment(LocalDate.of(year+1, 2, 28));
					instalments[3] = new Instalment(LocalDate.of(year+1, 5, 31));

					instalments[0].setAmount(firstInstalment);
					instalments[0].setNotes(1);
					
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
					String fileName = "DingleyRates-" + formatter.format(instalments[0].getDueDate()) + ".pdf";
					Document document = documentService.createDocumentFromEmailForExpense(response.body(), fileName);
					instalments[0].setDocument(document);
					
					CollectionUtil.splitAndConvert(content, "\n").stream().filter(line -> line.startsWith("$")).forEach(line -> {
						if (line.indexOf(firstInstalment) != -1) {
							foundFirst[0] = true;
						}
						else if (foundFirst[0] && counter[0] <= 3) {
							instalments[counter[0]].setAmount(line.replace("$", ""));
							instalments[counter[0]].setNotes(counter[0] + 1);
							counter[0]++;
						}
					});
					Arrays.asList(instalments).stream().forEach(item -> {
						createExpense(refData, item.getDueDate(), item.getDocument(), item.getNotes(), item.getAmount());
					});
				}
			}
		}
	}

}
