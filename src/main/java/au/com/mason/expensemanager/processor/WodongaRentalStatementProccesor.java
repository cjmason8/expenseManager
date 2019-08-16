package au.com.mason.expensemanager.processor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	
	private static List<String> FIRST_SIX_MONTHS;
	
	@Autowired
	protected NotificationService notificationService;
	
	@Autowired
	private RentalPaymentService rentalPaymentService;
	
	static {
		FIRST_SIX_MONTHS = new ArrayList<>();
		FIRST_SIX_MONTHS.add("Jan");
		FIRST_SIX_MONTHS.add("Feb");
		FIRST_SIX_MONTHS.add("Mar");
		FIRST_SIX_MONTHS.add("Apr");
		FIRST_SIX_MONTHS.add("May");
		FIRST_SIX_MONTHS.add("Jun");
	}

	@Override
	public void execute(Message message, RefData refData) throws Exception {
		
		if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			int count = mimeMultipart.getCount();
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = mimeMultipart.getBodyPart(i);
				if (bodyPart.isMimeType("text/html")) {
				} else if (bodyPart.getContentType().startsWith("APPLICATION/OCTET-STREAM")) {
					BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getContent();
					byte[] byteArray = IOUtils.toByteArray(base64DecoderStream);
					String fileName = message.getSubject().substring(0, message.getSubject().indexOf("(") - 1) + ".pdf";
					String folder = fileName.substring(fileName.indexOf(" to ") + 4).replace(".pdf", "");
					String month = folder.substring(folder.indexOf(" "), folder.lastIndexOf(" "));
					String year = folder.substring(folder.lastIndexOf(" ") + 1);
					if (FIRST_SIX_MONTHS.contains(month)) {
						folder = (Integer.valueOf(year) - 1) + "-" + year; 
					}
					else {
						folder = year + "-" + (Integer.valueOf(year) + 1);
					}
					Map<String, Object> metaData = new HashMap<>();
					metaData.put("property", "Wodonga");
					metaData.put("year", folder);
//					documentService.createDocumentForRentalStatement(byteArray, fileName,
//							"/Wodonga/" + folder + "/Statements", metaData);
					
					Notification notification = new Notification();
					notification.setMessage("Uploaded Wodonga rental statement - " + fileName);
					LOGGER.info("Uploaded Wodonga rental statement - " + fileName);
					
					RentalPayment rentalPayment = new RentalPayment();
					
					String content = PdfReader.extract(byteArray);
					CollectionUtil.splitAndConvert(content, "\n").stream().forEach(line -> {
						if (line.indexOf("Rent") != -1) {
							rentalPayment.setTotalRent(new BigDecimal(line.substring(1, line.indexOf(" "))));
							String dateString = line.substring(line.indexOf("Rent") + 5);
							String[] dates = dateString.split(" ");
				        	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
				        	rentalPayment.setStatementFrom(LocalDate.parse(dates[0], dateFormatter));
				        	rentalPayment.setStatementTo(LocalDate.parse(dates[1], dateFormatter));
						}
					});
					
//					rentalPaymentService.createDonation(rentalPayment);
//					
//					notificationService.create(notification);
				}
			}
		}
	}

}
