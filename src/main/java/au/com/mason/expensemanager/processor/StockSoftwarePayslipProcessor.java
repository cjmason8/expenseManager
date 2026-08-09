package au.com.mason.expensemanager.processor;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeUtility;

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

	private static final Pattern SUBJECT_PREFIX = Pattern.compile("(?i)^(Re|Fwd|Fw|Forward)\\s*:\\s*");

	private static final Pattern TRAILING_PAYSLIP_DATE = Pattern.compile("\\s*-\\s*\\d{1,2}-\\d{1,2}-\\d{4}$");

	@Autowired
	private StockSoftwarePayslipPdfParser payslipPdfParser;

	@Override
	public void execute(Message message, RefData refData) throws Exception {
		if (!EmailMessageParts.isMultipart(message)) {
			throw new IllegalStateException("Stock Software payslip email is not multipart");
		}

		BodyPart pdfPart = findPdfAttachment(message);
		byte[] pdfBytes = EmailMessageParts.readBytes(pdfPart);
		String attachmentFileName = decodeMailText(pdfPart.getFileName());

		PayslipData payslip = payslipPdfParser.parse(pdfBytes);
		String financialYear = RentalPaymentFinancialYear.financialYearLabel(payslip.payToDate());
		String subject = decodeMailText(message.getSubject());
		String fileName = buildFileName(subject, attachmentFileName, payslip.payToDate());
		LOGGER.info("Building payslip filename from subject='{}' attachment='{}' -> '{}'", subject,
			attachmentFileName, fileName);
		String folderPath = "/docs/expenseManager/filofax/Payslips/" + financialYear;

		Map<String, Object> metaData = buildDocumentMetadata(refData, financialYear);

		Document document = documentService.createDocumentFromEmailInFolder(pdfBytes, fileName, folderPath, metaData);

		Notification notification = new Notification();
		notification.setMessage("Payslip saved: " + fileName);
		notification.setDocumentFolderPath(folderPath);
		LOGGER.info("Uploaded Stock Software payslip to {} as {}", folderPath, document.getFileName());
		notificationService.create(notification);
	}

	private static BodyPart findPdfAttachment(Message message) throws MessagingException, java.io.IOException {
		for (BodyPart bodyPart : EmailMessageParts.allParts(message)) {
			if (EmailMessageParts.isPdfPart(bodyPart)) {
				return bodyPart;
			}
		}
		throw new IllegalStateException("Stock Software payslip email missing PDF attachment");
	}

	static Map<String, Object> buildDocumentMetadata(RefData refData, String financialYear) {
		Map<String, Object> metaData = new HashMap<>();
		if (refData.getMetaData() != null) {
			metaData.putAll(refData.getMetaData());
		}
		metaData.put("year", financialYear);
		return metaData;
	}

	static String buildFileName(String subject, String attachmentFileName, LocalDate payToDate) {
		String prefix = extractPayslipPrefix(subject);
		if ("Payslip".equals(prefix)) {
			String attachmentPrefix = extractPayslipPrefix(stripPdfExtension(attachmentFileName));
			if (!"Payslip".equals(attachmentPrefix)) {
				prefix = attachmentPrefix;
			}
		}
		return sanitizeFileName(prefix) + " - " + DateUtil.getFormattedDateString(payToDate) + ".pdf";
	}

	static String extractPayslipPrefix(String text) {
		if (text == null || text.isBlank()) {
			return "Payslip";
		}

		String trimmed = stripSubjectPrefixes(text.trim());
		if (trimmed.regionMatches(true, 0, "Payslip", 0, 7)) {
			return stripTrailingPayslipDate(trimmed);
		}

		return "Payslip";
	}

	private static String stripSubjectPrefixes(String subject) {
		String result = subject;
		while (SUBJECT_PREFIX.matcher(result).lookingAt()) {
			result = SUBJECT_PREFIX.matcher(result).replaceFirst("").trim();
		}
		return result;
	}

	private static String stripTrailingPayslipDate(String value) {
		return TRAILING_PAYSLIP_DATE.matcher(value).replaceFirst("").trim();
	}

	private static String stripPdfExtension(String fileName) {
		if (fileName == null) {
			return null;
		}
		String trimmed = fileName.trim();
		if (trimmed.regionMatches(true, trimmed.length() - 4, ".pdf", 0, 4)) {
			return trimmed.substring(0, trimmed.length() - 4).trim();
		}
		return trimmed;
	}

	private static String decodeMailText(String value) {
		if (value == null) {
			return null;
		}
		try {
			return MimeUtility.decodeText(value).trim();
		} catch (UnsupportedEncodingException e) {
			return value.trim();
		}
	}

	private static String sanitizeFileName(String value) {
		return value.replace("/", "-").replace("\\", "-").replace(":", "-").trim();
	}

}
