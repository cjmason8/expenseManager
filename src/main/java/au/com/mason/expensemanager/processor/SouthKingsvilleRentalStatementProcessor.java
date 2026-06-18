package au.com.mason.expensemanager.processor;

import java.util.HashMap;
import java.util.Map;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.Notification;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.domain.RentalPayment;
import au.com.mason.expensemanager.mail.EmailMessageParts;
import au.com.mason.expensemanager.pdf.rental.RentalStatementData;
import au.com.mason.expensemanager.pdf.rental.SouthKingsvilleRentalStatementPdfParser;
import au.com.mason.expensemanager.service.NotificationService;
import au.com.mason.expensemanager.service.RentalPaymentService;
import au.com.mason.expensemanager.util.RentalPaymentFinancialYear;

@Component
public class SouthKingsvilleRentalStatementProcessor extends Processor {

	private static final Logger LOGGER = LogManager.getLogger(SouthKingsvilleRentalStatementProcessor.class);

	@Autowired
	protected NotificationService notificationService;

	@Autowired
	private RentalPaymentService rentalPaymentService;

	@Autowired
	private SouthKingsvilleRentalStatementPdfParser rentalStatementPdfParser;

	@Override
	public void execute(Message message, RefData refData) throws Exception {
		if (!EmailMessageParts.isMultipart(message)) {
			return;
		}

		for (BodyPart bodyPart : EmailMessageParts.allParts(message)) {
			if (EmailMessageParts.isHtmlPart(bodyPart) || !EmailMessageParts.isPdfPart(bodyPart)) {
				continue;
			}

			byte[] pdfBytes = EmailMessageParts.readBytes(bodyPart);
			RentalStatementData statement = rentalStatementPdfParser.parse(pdfBytes);
			RentalPayment rentalPayment = toRentalPayment(statement);

			if (!statement.isBalanced()) {
				Notification unbalancedNotification = new Notification();
				unbalancedNotification.setMessage("There was an unbalanced Rental Payment for South Kingsville "
					+ rentalPayment.getStatementFrom() + " to " + rentalPayment.getStatementTo());
				notificationService.create(unbalancedNotification);
			}

			String fileName = message.getSubject().substring(0, message.getSubject().indexOf("from") - 1) + ".pdf";
			int financialYearEnd = RentalPaymentFinancialYear.financialYearEnd(rentalPayment.getStatementTo());
			String folder = financialYearEnd - 1 + "-" + financialYearEnd;

			Map<String, Object> metaData = new HashMap<>();
			metaData.put("property", "South Kingsville");
			metaData.put("year", folder);
			Document document = documentService.createDocumentForRentalStatement(pdfBytes, fileName,
				"/South Kingsville/" + folder + "/Statements", metaData);
			rentalPayment.setDocument(document);

			Notification notification = new Notification();
			notification.setMessage("Uploaded South Kingsville rental statement - " + fileName);
			LOGGER.info("Uploaded South Kingsville rental statement - {}", fileName);

			rentalPaymentService.createRentalPayment(rentalPayment);
			notificationService.create(notification);
		}
	}

	private RentalPayment toRentalPayment(RentalStatementData statement) {
		RentalPayment rentalPayment = new RentalPayment();
		rentalPayment.setProperty("STH_KINGSVILLE");
		rentalPayment.setTotalRent(statement.totalRent());
		rentalPayment.setManagementFee(statement.managementFee());
		rentalPayment.setAdminFee(statement.adminFee());
		rentalPayment.setStatementFrom(statement.statementFrom());
		rentalPayment.setStatementTo(statement.statementTo());
		return rentalPayment;
	}

}
