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
import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.html.dingley.DingleyRatesHtmlParser;
import au.com.mason.expensemanager.html.rates.RatesFirstNoticeHtmlData;
import au.com.mason.expensemanager.mail.EmailMessageParts;
import au.com.mason.expensemanager.pdf.rates.DingleyRatesFirstNoticePdfParser;
import au.com.mason.expensemanager.pdf.rates.DingleyRatesInstalmentNoticePdfParser;
import au.com.mason.expensemanager.pdf.rates.RatesInstalmentData;
import au.com.mason.expensemanager.pdf.rates.RatesInstalmentNoticeData;

@Component
public class DingleyRatesProcessor extends Processor {

	@Autowired
	private DingleyRatesInstalmentNoticePdfParser instalmentNoticePdfParser;

	@Autowired
	private DingleyRatesFirstNoticePdfParser firstNoticePdfParser;

	@Autowired
	private DingleyRatesHtmlParser dingleyRatesHtmlParser;

	@Override
	public void execute(Message message, RefData refData) throws Exception {
		if (message.getSubject().indexOf("Instalment") != -1) {
			handleInstalments(message, refData);
		} else {
			handleFirst(message, refData);
		}
	}

	private void handleInstalments(Message message, RefData refData)
		throws MessagingException, IOException, InterruptedException, Exception {
		if (!EmailMessageParts.isMultipart(message)) {
			return;
		}

		for (BodyPart bodyPart : EmailMessageParts.allParts(message)) {
			processInstalmentHtml(refData, bodyPart);
		}
	}

	private void processInstalmentHtml(RefData refData, BodyPart bodyPart)
		throws MessagingException, IOException, InterruptedException, Exception {
		if (!EmailMessageParts.isHtmlPart(bodyPart)) {
			return;
		}

		String html = EmailMessageParts.readHtml(bodyPart);
		byte[] pdfBytes = downloadRatesPdf(dingleyRatesHtmlParser.extractPdfDownloadUrl(html));
		RatesInstalmentNoticeData notice = instalmentNoticePdfParser.parse(pdfBytes);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
		String fileName = "DingleyRates-" + formatter.format(notice.dueDate()) + ".pdf";
		Document document = documentService.createDocumentFromEmailForExpense(pdfBytes, fileName);
		updateExpense(refData, notice.dueDate(), notice.amount(), document, null);
	}

	private void handleFirst(Message message, RefData refData)
		throws MessagingException, IOException, InterruptedException, Exception {
		if (!EmailMessageParts.isMultipart(message)) {
			return;
		}

		for (BodyPart bodyPart : EmailMessageParts.allParts(message)) {
			if (!EmailMessageParts.isHtmlPart(bodyPart)) {
				continue;
			}

			String html = EmailMessageParts.readHtml(bodyPart);
			RatesFirstNoticeHtmlData firstNotice = dingleyRatesHtmlParser.parseFirstNotice(html);
			byte[] pdfBytes = downloadRatesPdf(dingleyRatesHtmlParser.extractPdfDownloadUrl(html));
			List<RatesInstalmentData> instalments = firstNoticePdfParser.parse(pdfBytes,
				firstNotice.firstInstalmentAmount(), firstNotice.year());
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
			String fileName = "DingleyRates-" + formatter.format(instalments.get(0).dueDate()) + ".pdf";
			Document document = documentService.createDocumentFromEmailForExpense(pdfBytes, fileName);

			for (RatesInstalmentData instalment : instalments) {
				Document instalmentDocument = instalment.instalmentNumber() == 1 ? document : null;
				createExpense(refData, instalment.dueDate(), instalmentDocument, instalment.notes(),
					instalment.amountAsBigDecimal());
			}
		}
	}

	private byte[] downloadRatesPdf(String url) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newBuilder().version(Version.HTTP_2).followRedirects(Redirect.ALWAYS).build();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofMinutes(1)).GET()
			.build();
		HttpResponse<byte[]> response = client.send(request, BodyHandlers.ofByteArray());
		return response.body();
	}

}
