package au.com.mason.expensemanager.robot;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.FlagTerm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Notification;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.mail.GmailMailSupport;
import au.com.mason.expensemanager.processor.EmailProcessor;
import au.com.mason.expensemanager.service.AwsSecretsService;
import au.com.mason.expensemanager.service.NotificationService;
import au.com.mason.expensemanager.service.RefDataService;

@Component
public class EmailTrawler {

	private static final Logger LOGGER = LogManager.getLogger(EmailTrawler.class);
	private static final String GMAIL_HOST = "imap.gmail.com";
	private static final String MAIL_PROTOCOL = "imaps";
	private static final String INBOX_FOLDER = "INBOX";
	private static final String STOCK_SOFTWARE_LABEL_FOLDER = "StockSoftware";

	private static final List<String> BLACKLISTED_EMAILS = List.of("tripadvisor", "roses", "puzzles", "youtube",
		"messages.telstra.com", "storm", "marvel", "paypal", "tennis", "mightymunch");

	private final AwsSecretsService awsSecretsService;
	private final RefDataService refDataService;
	private final NotificationService notificationService;

	@Value("${email.secret.name:email-credentials}")
	private String emailSecretName;

	@Value("${email.payslip.secret.name:email-payslip-credentials}")
	private String payslipEmailSecretName;

	@Autowired
	public EmailTrawler(AwsSecretsService awsSecretsService, RefDataService refDataService,
		NotificationService notificationService) {
		this.awsSecretsService = awsSecretsService;
		this.refDataService = refDataService;
		this.notificationService = notificationService;
	}

	public EmailTrawlerResult check() {
		EmailTrawlerResult result = new EmailTrawlerResult();

		LOGGER.info("EmailTrawler starting");
		result.addDetail("EmailTrawler started");

		List<RefData> refDatas = refDataService.getAllWithEmailKey();
		result.addDetail("Loaded " + refDatas.size() + " email processor(s) from refdata");

		List<RefData> inboxRefDatas = refDatas.stream().filter(refData -> !isPayslipProcessor(refData))
			.collect(Collectors.toList());
		List<RefData> payslipRefDatas = refDatas.stream().filter(this::isPayslipProcessor).collect(Collectors.toList());

		processMailbox(emailSecretName, "bills", store -> processFolder(store, INBOX_FOLDER, inboxRefDatas, result),
			result);

		if (payslipRefDatas.isEmpty()) {
			result.addDetail("No payslip processor configured in refdata; skipping payslip mailbox");
		} else {
			processMailbox(payslipEmailSecretName, "payslip",
				store -> processLabelFolder(store, STOCK_SOFTWARE_LABEL_FOLDER, payslipRefDatas, result), result);
		}

		LOGGER.info("EmailTrawler finished: {}", result.summary());
		result.addDetail("EmailTrawler finished");
		return result;
	}

	private void processMailbox(String secretName, String mailboxDescription, Consumer<Store> mailboxAction,
		EmailTrawlerResult result) {
		Store store = null;

		try {
			String user = awsSecretsService.getSecretValue(secretName, "USER_NAME");
			String password = awsSecretsService.getSecretValue(secretName, "PASSWORD");

			Session emailSession = createEmailSession();
			store = emailSession.getStore(MAIL_PROTOCOL);
			store.connect(GMAIL_HOST, user, password);
			result.addDetail("Connected to " + mailboxDescription + " Gmail as " + user);

			mailboxAction.accept(store);

		} catch (Exception e) {
			String message = "Error checking " + mailboxDescription + " mailbox (" + secretName + "): "
				+ (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
			LOGGER.error(message, e);
			result.addDetail(message);
			result.fail(message);
		} finally {
			closeStore(store);
		}
	}

	private boolean isPayslipProcessor(RefData refData) {
		return refData.getEmailProcessor() != null
			&& refData.getEmailProcessor().equals(EmailProcessor.STOCK_SOFTWARE_PAYSLIP);
	}

	private void processLabelFolder(Store store, String labelName, List<RefData> refDatas, EmailTrawlerResult result) {
		try {
			Optional<String> folderName = GmailMailSupport.findLabelFolder(store, labelName);
			if (folderName.isPresent()) {
				processFolder(store, folderName.get(), refDatas, result);
				return;
			}

			String message = "Email folder not found for Gmail label: " + labelName
				+ ". Enable 'Show in IMAP' for this label in Gmail settings (Settings -> Labels).";
			LOGGER.warn(message);
			result.addDetail(message);

			List<String> visibleLabels = GmailMailSupport.listLabelFolderNames(store);
			if (!visibleLabels.isEmpty()) {
				result.addDetail("IMAP-visible labels: " + String.join(", ", visibleLabels));
			} else {
				result.addDetail("No custom IMAP-visible labels found on this mailbox");
			}

			Message[] labelledUnread;
			Folder allMail = store.getFolder("[Gmail]/All Mail");
			if (!allMail.exists()) {
				result.addDetail("Gmail All Mail folder not found");
				return;
			}

			allMail.open(Folder.READ_WRITE);
			try {
				labelledUnread = GmailMailSupport.fetchUnreadFromAllMailWithLabel(allMail, labelName);
				if (labelledUnread.length == 0) {
					result.addDetail("No unread messages with X-GM-LABELS containing " + labelName + " in All Mail");
					return;
				}

				LOGGER.info("Processing {} unread labelled messages for {} from All Mail", labelledUnread.length,
					labelName);
				result.addDetail("Processing " + labelledUnread.length + " unread labelled message(s) for " + labelName
					+ " from All Mail");
				processMessages(labelledUnread, refDatas, labelName, result);
			} finally {
				closeFolder(allMail);
			}

		} catch (Exception e) {
			String message = "Error resolving Gmail label " + labelName + ": "
				+ (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
			LOGGER.error(message, e);
			result.addDetail(message);
			result.fail(message);
		}
	}

	private void processFolder(Store store, String folderName, List<RefData> refDatas, EmailTrawlerResult result) {
		Folder emailFolder = null;

		try {
			emailFolder = store.getFolder(folderName);
			if (!emailFolder.exists()) {
				String message = "Email folder not found: " + folderName;
				LOGGER.warn(message);
				result.addDetail(message);
				return;
			}

			emailFolder.open(Folder.READ_WRITE);

			Message[] messages = fetchUnreadMessages(emailFolder);
			if (messages.length == 0) {
				String message = "No unread messages in " + folderName;
				LOGGER.info(message);
				result.addDetail(message);
				return;
			}

			LOGGER.info("Processing {} unread messages in {}", messages.length, folderName);
			result.addDetail("Processing " + messages.length + " unread message(s) in " + folderName);
			processMessages(messages, refDatas, folderName, result);

		} catch (Exception e) {
			String message = "Error processing folder " + folderName + ": "
				+ (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
			LOGGER.error(message, e);
			result.addDetail(message);
			result.fail(message);
		} finally {
			closeFolder(emailFolder);
		}
	}

	private Session createEmailSession() {
		Properties properties = new Properties();
		properties.put("mail.store.protocol", MAIL_PROTOCOL);
		properties.put("mail.imaps.ssl.trust", GMAIL_HOST);
		properties.put("mail.imaps.ssl.protocols", "TLSv1.2");
		properties.put("mail.imaps.timeout", "10000");
		properties.put("mail.imaps.connectiontimeout", "10000");
		return Session.getInstance(properties);
	}

	private Message[] fetchUnreadMessages(Folder folder) throws MessagingException {
		Flags unseenFlag = new Flags(Flags.Flag.SEEN);
		FlagTerm unseenFlagTerm = new FlagTerm(unseenFlag, false);
		return folder.search(unseenFlagTerm);
	}

	private void processMessages(Message[] messages, List<RefData> refDatas, String folderName,
		EmailTrawlerResult result) {
		boolean payslipLabelFolder = isPayslipLabelFolder(folderName);

		for (Message message : messages) {
			try {
				String subject = message.getSubject();

				if (isBlacklisted(message)) {
					LOGGER.debug("Skipping blacklisted email from: {}", getFromAddress(message));
					if (payslipLabelFolder) {
						result.addDetail("Skipped blacklisted email in " + folderName + " (left unread)");
					} else {
						result.addDetail("Skipped blacklisted email in " + folderName);
						markAsRead(message);
					}
					continue;
				}

				if (payslipLabelFolder && !isPayslipSubject(subject)) {
					LOGGER.debug("Skipping non-payslip email in {}: {}", folderName, subject);
					result.addDetail("Skipped non-payslip email in " + folderName + " (left unread): " + subject);
					continue;
				}

				LOGGER.info("Processing email in {}: {}", folderName, subject);
				result.addDetail("Checking " + folderName + ": " + subject);

				boolean processed = processMessage(message, refDatas, folderName, result);

				if (!processed) {
					if (payslipLabelFolder) {
						result.addDetail("Skipped unhandled payslip email (left unread): " + subject);
					} else {
						createUnhandledNotification(subject);
						result.addDetail("Unhandled email in " + folderName + ": " + subject);
						markAsRead(message);
					}
					continue;
				}

				markAsRead(message);

			} catch (Exception e) {
				String errorDetail = "Error processing message in " + folderName + ": "
					+ (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
				LOGGER.error(errorDetail, e);
				result.addDetail(errorDetail + " (left unread)");
				result.fail(errorDetail);
			}
		}
	}

	private boolean isPayslipLabelFolder(String folderName) {
		return STOCK_SOFTWARE_LABEL_FOLDER.equals(folderName);
	}

	private boolean isPayslipSubject(String subject) {
		return subject != null && subject.contains("Payslip");
	}

	private boolean processMessage(Message message, List<RefData> refDatas, String folderName,
		EmailTrawlerResult result) throws MessagingException, IOException {
		for (RefData refData : refDatas) {
			if (refDataMatch(message, refData, folderName)) {
				String processorName = refData.getEmailProcessor().getProcessor().getClass().getSimpleName();
				LOGGER.info("Matched processor: {}", processorName);
				result.addDetail("Matched processor " + processorName + " for " + message.getSubject());
				try {
					refData.getEmailProcessor().getProcessor().execute(message, refData);
					result.addDetail("Processed " + message.getSubject() + " with " + processorName);
				} catch (Exception e) {
					LOGGER.error("Error executing processor", e);
					throw new IOException("Processor execution failed", e);
				}
				return true;
			}
		}
		return false;
	}

	private void createUnhandledNotification(String subject) {
		Notification notification = new Notification();
		notification.setMessage("Unhandled Email: " + subject);
		notificationService.create(notification);
	}

	private void closeFolder(Folder folder) {
		try {
			if (folder != null && folder.isOpen()) {
				folder.close(false);
			}
		} catch (MessagingException e) {
			LOGGER.warn("Error closing folder", e);
		}
	}

	private void closeStore(Store store) {
		try {
			if (store != null && store.isConnected()) {
				store.close();
			}
		} catch (MessagingException e) {
			LOGGER.warn("Error closing store", e);
		}
	}

	private void markAsRead(Message message) throws MessagingException {
		message.setFlag(Flags.Flag.SEEN, true);
	}

	private boolean isBlacklisted(Message message) throws MessagingException {
		String fromAddress = getFromAddress(message);
		if (fromAddress == null) {
			return false;
		}

		String lowerCaseFrom = fromAddress.toLowerCase();
		return BLACKLISTED_EMAILS.stream().anyMatch(lowerCaseFrom::contains);
	}

	private String getFromAddress(Message message) throws MessagingException {
		if (message.getFrom() != null && message.getFrom().length > 0) {
			return message.getFrom()[0].toString();
		}
		return null;
	}

	private boolean refDataMatch(Message message, RefData refData, String folderName)
		throws MessagingException, IOException {
		String subject = message.getSubject();
		if (subject == null) {
			return false;
		}

		String emailKey = refData.getEmailKey();
		EmailProcessor processor = refData.getEmailProcessor();

		if (processor != null && processor.equals(EmailProcessor.STOCK_SOFTWARE_PAYSLIP)) {
			return matchStockSoftwarePayslip(subject, folderName);
		}

		// Check for RACV-specific processing
		if (bodyContains(message, "RACV")) {
			return matchRACVEmail(message, subject, emailKey, processor);
		}

		// Default: simple subject match
		return subject.contains(emailKey);
	}

	private boolean matchStockSoftwarePayslip(String subject, String folderName) {
		return STOCK_SOFTWARE_LABEL_FOLDER.equals(folderName) && subject.contains("Payslip");
	}

	private boolean matchRACVEmail(Message message, String subject, String emailKey, EmailProcessor processor)
		throws MessagingException, IOException {

		if (processor.equals(EmailProcessor.RACV_MEMBERSHIP)) {
			String fromAddress = getEmailAddress(message);
			return subject.startsWith(emailKey) && fromAddress != null && fromAddress.startsWith("racvrenewal_noreply");
		}

		if (emailKey.equals("Your Renewal RACV Comprehensive")) {
			return matchRACVComprehensive(message, processor);
		}

		if (emailKey.equals("Your Renewal RACV Home Buildings Ins")) {
			return matchRACVHomeInsurance(message, processor);
		}

		if (processor.equals(EmailProcessor.DINGLEY_INSURANCE)) {
			return subject.startsWith(emailKey);
		}

		return false;
	}

	private boolean matchRACVComprehensive(Message message, EmailProcessor processor)
		throws MessagingException, IOException {
		if (processor.equals(EmailProcessor.CAMRY_INSURANCE)) {
			return bodyContains(message, "TOYOTA CAMRY");
		}
		if (processor.equals(EmailProcessor.FORD_INSURANCE)) {
			return bodyContains(message, "FORD FAIRMONT");
		}
		if (processor.equals(EmailProcessor.FORESTER_INSURANCE)) {
			return bodyContains(message, "SUBARU FORESTER");
		}
		return bodyContains(message, "MAZDA TRIBUTE");
	}

	private boolean matchRACVHomeInsurance(Message message, EmailProcessor processor)
		throws MessagingException, IOException {
		if (processor.equals(EmailProcessor.WODONGA_INSURANCE)) {
			return bodyContains(message, "WODONGA");
		}
		return bodyContains(message, "SOUTH KINGSVILLE");
	}

	private String getEmailAddress(Message message) throws MessagingException {
		if (message.getFrom() != null && message.getFrom().length > 0
			&& message.getFrom()[0] instanceof InternetAddress) {
			return ((InternetAddress) message.getFrom()[0]).getAddress();
		}
		return null;
	}

	private boolean bodyContains(Message message, String phrase) throws MessagingException, IOException {
		if (!message.isMimeType("multipart/*")) {
			return false;
		}

		try {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			int count = mimeMultipart.getCount();

			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = mimeMultipart.getBodyPart(i);
				if (bodyPart.isMimeType("text/html") || bodyPart.isMimeType("text/plain")) {
					Object content = bodyPart.getContent();
					if (content instanceof String) {
						String bodyContent = (String) content;
						if (bodyContent.contains(phrase)) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Error reading message body", e);
		}

		return false;
	}

}
