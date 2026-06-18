package au.com.mason.expensemanager.processor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

import jakarta.mail.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.html.BillDownloadNoticeData;
import au.com.mason.expensemanager.html.southkingsvillewater.SouthKingsvilleWaterBillHtmlParser;
import au.com.mason.expensemanager.mail.EmailMessageParts;

@Component
public class SouthKingsvilleWaterProcessor extends Processor {

	@Autowired
	private SouthKingsvilleWaterBillHtmlParser southKingsvilleWaterBillHtmlParser;

	@Override
	public void execute(Message message, RefData refData) throws Exception {
		if (!EmailMessageParts.isHtml(message)) {
			return;
		}

		BillDownloadNoticeData bill = southKingsvilleWaterBillHtmlParser
			.parse(EmailMessageParts.htmlBody(message).orElseThrow());

		HttpClient client = HttpClient.newBuilder().version(Version.HTTP_2).followRedirects(Redirect.ALWAYS)
			.build();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(bill.downloadUrl()))
			.timeout(Duration.ofMinutes(1)).GET().build();
		HttpResponse<byte[]> response = client.send(request, BodyHandlers.ofByteArray());

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
		String fileName = "SouthKingsvilleWater-" + formatter.format(bill.dueDate()) + ".pdf";
		Document document = documentService.createDocumentFromEmailForExpense(response.body(), fileName);

		updateExpense(refData, bill.dueDate(), bill.amount(), document, null);
	}

}
