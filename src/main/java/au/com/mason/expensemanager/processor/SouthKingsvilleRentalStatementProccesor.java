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

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sun.mail.util.BASE64DecoderStream;

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
				} else if (bodyPart.getContentType().startsWith("APPLICATION/PDF")) {
					BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getContent();
					byte[] byteArray = IOUtils.toByteArray(base64DecoderStream);
					
					RentalPayment rentalPayment = new RentalPayment();
					
					rentalPayment.setProperty("STH_KINGSVILLE");
		        	
					String content = PdfReader.extract(byteArray);
					
					BigDecimal[] paymentToOwner = new BigDecimal[1];
					CollectionUtil.splitAndConvert(content, "\n").stream().forEach(line -> {
						if (line.startsWith("Money In")) {
							rentalPayment.setTotalRent(new BigDecimal(line.substring(line.indexOf("$") + 1).replace(",", ""))); 
						} 
						else if (line.indexOf("Management fee") != -1) {
							rentalPayment.setManagementFee(new BigDecimal(line.substring(line.indexOf("*") + 3).replace(",", ""))); 
						} 
						else if (line.indexOf("Accounting Fee") != -1) { 
							rentalPayment.setAdminFee(new BigDecimal(line.substring(line.indexOf("*") + 3).replace(",", ""))); 
						} 
						else if (line.indexOf("Rent paid to") != -1) { 
							String endDate = line.substring(13, line.indexOf("(") - 1); 
							int index = 20; 
							if (line.indexOf("moved in") != -1) {
								index = 10; 
							} 
							
							String startDate = line.substring(line.indexOf("(") + index, line.indexOf(")")); 
							DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
							rentalPayment.setStatementFrom(LocalDate.parse(startDate, dateFormatter));
							rentalPayment.setStatementTo(LocalDate.parse(endDate, dateFormatter)); 
						} else if (line.indexOf("You Received") != -1) { 
							paymentToOwner[0] = new BigDecimal(line.substring(line.indexOf("$") + 1).replace(",", "")); 
						} 
					});

					if (paymentToOwner[0].compareTo(rentalPayment.getPaymentToOwner()) != 0) {
						Notification unbalancedRemtalNotification = new Notification();
						unbalancedRemtalNotification.setMessage("There was an unbalanced Rental Payment for South Kingsville " +
								rentalPayment.getStatementFrom() + " to " + rentalPayment.getStatementTo());
						notificationService.create(unbalancedRemtalNotification); 
					}

					String fileName = message.getSubject().substring(0, message.getSubject().indexOf("from") - 1) + ".pdf"; 
					int month = rentalPayment.getStatementTo().getMonth().getValue(); 
					String year = String.valueOf(rentalPayment.getStatementTo().getYear()); 
					String folder = "";
					if (month <= 6) { 
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
