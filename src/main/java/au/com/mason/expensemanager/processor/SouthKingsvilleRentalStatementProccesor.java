package au.com.mason.expensemanager.processor;

import java.math.BigDecimal;
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
import java.util.HashMap;
import java.util.Map;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.Notification;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.domain.RentalPayment;
import au.com.mason.expensemanager.pdf.PdfReader;
import au.com.mason.expensemanager.service.NotificationService;
import au.com.mason.expensemanager.service.RentalPaymentService;
import au.com.mason.expensemanager.util.CollectionUtil;

@Component
public class SouthKingsvilleRentalStatementProccesor extends Processor {

	private static Logger LOGGER = LogManager.getLogger(SouthKingsvilleRentalStatementProccesor.class);

	@Autowired
	protected NotificationService notificationService;

	@Autowired
	private RentalPaymentService rentalPaymentService;

	@Override
	public void execute(Message message, RefData refData) throws Exception {

		if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			int count = mimeMultipart.getCount();
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = mimeMultipart.getBodyPart(i);
				if (bodyPart.isMimeType("text/html")) {
					String body = (String) bodyPart.getContent();
					int startIndex = body.indexOf("https", body.indexOf("https") + 5);
					String url = body.substring(startIndex, body.indexOf("target", startIndex) - 2);

					HttpClient client = HttpClient.newBuilder().version(Version.HTTP_2).followRedirects(Redirect.ALWAYS)
							.build();

					HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofMinutes(1))
							.header("Content-Type", "application/json").GET().build();

					HttpResponse<byte[]> response = client.send(request, BodyHandlers.ofByteArray());
					byte[] byteArray = response.body();
					
					RentalPayment rentalPayment = new RentalPayment();
					//rentalPayment.setDocument(document);
					rentalPayment.setProperty("STH_KINGSVILLE");

					String content = PdfReader.extract(byteArray);
					CollectionUtil.splitAndConvert(content, "\n").stream().forEach(line -> { 
						if (line.startsWith("Money In")) {
							rentalPayment.setTotalRent(new BigDecimal(line.substring(line.indexOf("$") + 1).replace(",", ""))); 
						} 
						else if	(line.indexOf("Management fee") != -1) { 
							rentalPayment.setManagementFee(new BigDecimal(line.substring(line.indexOf("*") + 3).replace(",", ""))); 
						} 
						else if (line.indexOf("Accounting Fee") != -1) 
						{ 
							rentalPayment.setAdminFee(new BigDecimal(line.substring(line.indexOf("*") + 3).replace(",", ""))); 
						}
						else if (line.indexOf("Rent paid to") != -1) 
						{ 
							String endDate = line.substring(13, line.indexOf("(") - 1);
							String startDate = line.substring(line.indexOf("(") + 20, line.indexOf(")"));
							DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
				        	rentalPayment.setStatementFrom(LocalDate.parse(startDate, dateFormatter));
				        	rentalPayment.setStatementTo(LocalDate.parse(endDate, dateFormatter)); 
						}
						else if (line.indexOf("You Received") != -1) { 
							BigDecimal paymentToOwner = new BigDecimal(line.substring(line.indexOf("$") + 1).replace(",", ""));
							if (paymentToOwner.compareTo(rentalPayment.getPaymentToOwner()) != 0) {
								Notification unbalancedRemtalNotification = new Notification();
								unbalancedRemtalNotification.setMessage("There was an unbalanced Rental Payment for South Kingsville " + rentalPayment.getStatementFrom() + " to " + rentalPayment.getStatementTo());
								notificationService.create(unbalancedRemtalNotification); 
							} 
						} 
					});
					
					String fileName = message.getSubject().substring(0, message.getSubject().indexOf("from") - 1) + ".pdf";
					String month = String.valueOf(rentalPayment.getStatementTo().getMonth());
					String year = String.valueOf(rentalPayment.getStatementTo().getYear());
					String folder = "";
					if (RentalPaymentService.FIRST_SIX_MONTHS.contains(month)) {
						folder = (Integer.valueOf(year) - 1) + "-" + year; 
					}
					else {
						folder = year + "-" + (Integer.valueOf(year) + 1);
					}
					Map<String, Object> metaData = new HashMap<>();
					metaData.put("property", "South Kingsville");
					metaData.put("year", folder);
					Document document = documentService.createDocumentForRentalStatement(byteArray, fileName,
							"/South Kingsville/" + folder + "/Statements", metaData);
					rentalPayment.setDocument(document);
					
					Notification notification = new Notification();
					notification.setMessage("Uploaded South Kingsville rental statement - " + fileName);
					LOGGER.info("Uploaded South Kingsville rental statement - " + fileName);
					
					rentalPaymentService.createRentalPayment(rentalPayment);
					notificationService.create(notification);
				}
			}
		}
	}

}