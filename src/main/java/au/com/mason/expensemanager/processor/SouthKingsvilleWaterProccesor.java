package au.com.mason.expensemanager.processor;

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
import java.time.format.DateTimeFormatterBuilder;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.pdf.PdfReader;
import au.com.mason.expensemanager.util.CollectionUtil;

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
			String[] amount = new String[1];
			DateTimeFormatter dueDateFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd MMM yyyy").toFormatter();
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = mimeMultipart.getBodyPart(i);
				if (bodyPart.isMimeType("text/html")) {
					body = (String) bodyPart.getContent();
					String url = body.substring(body.indexOf("https://viewpoint"), body.indexOf("\"", body.indexOf("https://viewpoint")));
					int start = body.indexOf(">By") + 4;
					dueDate = LocalDate.parse(body.substring(start, body.indexOf("<", start)), dueDateFormatter);

					HttpClient client = HttpClient.newBuilder().version(Version.HTTP_2).followRedirects(Redirect.ALWAYS)
							.build();

					HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofMinutes(1))
							.GET().build();

					HttpResponse<byte[]> response = client.send(request, BodyHandlers.ofByteArray());
					
					String content = PdfReader.extract(response.body());
					
					CollectionUtil.splitAndConvert(content, "\n").stream().forEach(line -> {
						if (line.indexOf("PLEASE PAY") != -1) {
							amount[0] = line.substring(line.indexOf("$") + 1);
						}
					});
					
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
					
					String fileName = "SouthKingsvilleWater-" + formatter.format(dueDate) + ".pdf";
					Document document = documentService.createDocumentFromEmailForExpense(response.body(), fileName);

					updateExpense(refData, dueDate, amount[0], document, null);
				}
			}
		}
	}

}
