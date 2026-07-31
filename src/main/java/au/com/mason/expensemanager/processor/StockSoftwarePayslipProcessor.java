package au.com.mason.expensemanager.processor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.Notification;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.mail.EmailMessageParts;
import au.com.mason.expensemanager.pdf.payslip.PayslipData;
import au.com.mason.expensemanager.pdf.payslip.StockSoftwarePayslipPdfParser;
import au.com.mason.expensemanager.util.DateUtil;
import au.com.mason.expensemanager.util.RentalPaymentFinancialYear;

@Component
public class StockSoftwarePayslipProcessor extends Processor {

	private static final Logger LOGGER = LogManager.getLogger(StockSoftwarePayslipProcessor.class);

	@Autowired
	private StockSoftwarePayslipPdfParser payslipPdfParser;

	@Override
	public void execute(Message message, RefData refData) throws Exception {
		if (!EmailMessageParts.isMultipart(message)) {
			throw new IllegalStateException("Stock Software payslip email is not multipart");
		}

		byte[] pdfBytes = EmailMessageParts.firstMatchingAttachment(message, bodyPart -> {
			try {
				return EmailMessageParts.isPdfPart(bodyPart);
			} catch (MessagingException e) {
				throw new IllegalStateException("Failed to inspect email attachment", e);
			}
		}).orElseThrow(() -> new IllegalStateException("Stock Software payslip email missing PDF attachment"));

		PayslipData payslip = payslipPdfParser.parse(pdfBytes);
		String financialYear = RentalPaymentFinancialYear.financialYearLabel(payslip.payToDate());
		String fileName = buildFileName(message.getSubject(), payslip.payToDate());
		String folderPath = "/docs/expenseManager/filofax/Payslips/" + financialYear;

		Map<String, Object> metaData = buildDocumentMetadata(refData, financialYear);

		Document document = documentService.createDocumentFromEmailInFolder(pdfBytes, fileName, folderPath, metaData);

		Notification notification = new Notification();
		notification.setMessage("Payslip saved: " + fileName);
		notification.setDocumentFolderPath(folderPath);
		LOGGER.info("Uploaded Stock Software payslip to {} as {}", folderPath, document.getFileName());
		notificationService.create(notification);
	}

	static Map<String, Object> buildDocumentMetadata(RefData refData, String financialYear) {
		Map<String, Object> metaData = new HashMap<>();
		if (refData.getMetaData() != null) {
			metaData.putAll(refData.getMetaData());
		}
		metaData.put("year", financialYear);
		return metaData;
	}

	static String buildFileName(String subject, LocalDate payToDate) {
		String prefix = "Payslip";
		if (subject != null) {
			String trimmed = subject.trim();
			if (trimmed.startsWith("Payslip")) {
				prefix = trimmed;
			}
		}
		return sanitizeFileName(prefix) + " - " + DateUtil.getFormattedDateString(payToDate) + ".pdf";
	}

	private static String sanitizeFileName(String value) {
		return value.replace("/", "-").replace("\\", "-").replace(":", "-").trim();
	}

}
