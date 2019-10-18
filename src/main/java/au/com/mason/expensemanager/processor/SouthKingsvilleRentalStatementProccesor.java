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

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
					// System.out.println(response.statusCode());
					byte[] byteArray = response.body();
					
					RentalPayment rentalPayment = new RentalPayment();
					//rentalPayment.setDocument(document);
					rentalPayment.setProperty("STH_KINGSVILLE");
					String dateString = message.getSubject().substring(message.getSubject().indexOf("-") + 2, message.getSubject().indexOf("(") - 1);
					String[] dates = dateString.split(" to ");
		        	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy");
		        	rentalPayment.setStatementFrom(LocalDate.parse(dates[0], dateFormatter));
		        	rentalPayment.setStatementTo(LocalDate.parse(dates[1], dateFormatter));

					String content = PdfReader.extract(byteArray);
					System.out.println(content);
					CollectionUtil.splitAndConvert(content, "\n").stream().forEach(line -> { 
						if (line.indexOf("Rent") != -1) { 
							rentalPayment.setTotalRent(new BigDecimal(line.substring(1, line.indexOf(" ")))); 
						} 
						else if	(line.indexOf("Management Fee") != -1) { 
							rentalPayment.setManagementFee(new BigDecimal(line.substring(1, line.indexOf(".") + 3))); 
						} 
						else if (line.indexOf("Administration Fee") != -1) 
						{ 
							rentalPayment.setAdminFee(new BigDecimal(line.substring(1, line.indexOf(".") + 3))); 
						} 
						else if (line.indexOf("Payment to Owner") != -1) { 
							BigDecimal paymentToOwner = new BigDecimal(line.substring(1, line.indexOf(".") + 3)); 
							if (paymentToOwner.compareTo(rentalPayment.getPaymentToOwner()) != 0) {
								Notification unbalancedRemtalNotification = new Notification();
								unbalancedRemtalNotification.setMessage("There was an unbalanced Rental Payment for Wodonga " + rentalPayment.getStatementFrom() + " to " + rentalPayment.getStatementTo());
								notificationService.create(unbalancedRemtalNotification); 
							} 
						} 
					});
				}
			}
		}
	}

}
