package au.com.mason.expensemanager.processor;

import java.math.BigDecimal;
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
public class WodongaRentalStatementProccesor extends Processor {
	
	private static Logger LOGGER = LogManager.getLogger(WodongaRentalStatementProccesor.class);
	
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
					
					rentalPayment.setProperty("WODONGA");
		        	
		        	Notification notification = new Notification();
					
					String content = PdfReader.extract(byteArray);
					Boolean[] found = new Boolean[2];
					found[0] = false;
					found[1] = false;
					CollectionUtil.splitAndConvert(content, "\n").stream().forEach(line -> {
						if (line.indexOf("Total\tincome") != -1) {
							rentalPayment.setTotalRent(new BigDecimal(line.substring(line.indexOf("$") + 1)));
						}
						else if (line.indexOf("Rent\tCommission") != -1) {
							rentalPayment.setManagementFee(new BigDecimal(line.substring(line.indexOf("$") + 1)));
						}
						else if (line.indexOf("Sundry\tFee") != -1) {
							if (line.indexOf("$") == -1) {
								found[1] = true;
							}
							else {
								rentalPayment.setAdminFee(new BigDecimal(line.substring(line.indexOf("$") + 1)));
							}
						}
						else if (found[1]) {
							found[1] = false;
							rentalPayment.setAdminFee(new BigDecimal(line.substring(line.indexOf("$") + 1).replace(")", "")));
						}
						else if (line.indexOf("Payment\tto\towner") != -1) {
							found[0] = true;
						}
						else if (line.startsWith("Statement\tperiod")) {
							String[] dates = line.replace("Statement\tperiod ", "").split("\t-\t");
							DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
				        	rentalPayment.setStatementFrom(LocalDate.parse(dates[0].replace("\t", " "), dateFormatter));
				        	rentalPayment.setStatementTo(LocalDate.parse(dates[1].replace("\t", " "), dateFormatter));		
						}
						else if (found[0]) {
							found[0] = false;
							BigDecimal paymentToOwner = new BigDecimal(line.substring(line.indexOf("$") + 1));
							if (paymentToOwner.compareTo(rentalPayment.getPaymentToOwner()) != 0) {
								Notification unbalancedRemtalNotification = new Notification();
								notification.setMessage("There was an unbalanced Rental Payment for Wodonga " + rentalPayment.getStatementFrom() + " to " + rentalPayment.getStatementTo());
								notificationService.create(unbalancedRemtalNotification);	
							}
						}
					});
					
					String fileName = message.getSubject().replace(": ", "-").replace("/", "-") + ".pdf";
					String folder = "";
					
					if (rentalPayment.getStatementFrom().getMonthValue() <= 6) {
						folder = (rentalPayment.getStatementFrom().getYear() - 1) + "-" + rentalPayment.getStatementFrom().getYear(); 
					}
					else {
						folder = rentalPayment.getStatementFrom().getYear() + "-" + (rentalPayment.getStatementFrom().getYear() + 1);
					}
					Map<String, Object> metaData = new HashMap<>();
					metaData.put("property", "Wodonga");
					metaData.put("year", folder);
					Document document = documentService.createDocumentForRentalStatement(byteArray, fileName,
							"/Wodonga/" + folder + "/Statements", metaData);
					
					rentalPayment.setDocument(document);
					
					notification.setMessage("Uploaded Wodonga rental statement - " + fileName);
					
					LOGGER.info("Uploaded Wodonga rental statement - " + fileName);
					
					rentalPaymentService.createRentalPayment(rentalPayment);
					notificationService.create(notification);
				}
			}
		}
	}

}
