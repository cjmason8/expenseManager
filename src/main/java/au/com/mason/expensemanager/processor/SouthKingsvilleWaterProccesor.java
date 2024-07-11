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
		LocalDate dueDate = null;
		String amount = null;
		if (message.isMimeType("text/html")) {
			body = (String) message.getContent();
			String urlContent = body.substring(0, body.indexOf("View my bill"));
			String url = urlContent.substring(urlContent.lastIndexOf("https"), urlContent.lastIndexOf("style") - 2);
			String dateContent = body.substring(body.indexOf("Pay by") + 13);
			dateContent = dateContent.substring(0, dateContent.indexOf("</span"));
			dueDate = LocalDate.parse(dateContent.substring(dateContent.lastIndexOf(">") + 1), DateTimeFormatter.ofPattern("dd LLL yyyy"));
			amount = body.substring(body.indexOf("$") + 1, body.indexOf("<", body.indexOf("$")));

			HttpClient client = HttpClient.newBuilder().version(Version.HTTP_2).followRedirects(Redirect.ALWAYS)
					.build();

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofMinutes(1))
					.GET().build();

			HttpResponse<byte[]> response = client.send(request, BodyHandlers.ofByteArray());

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");

			String fileName = "SouthKingsvilleWater-" + formatter.format(dueDate) + ".pdf";
			Document document = documentService.createDocumentFromEmailForExpense(response.body(), fileName);

			updateExpense(refData, dueDate, amount, document, null);
		}
	}
}
