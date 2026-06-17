package au.com.mason.expensemanager.robot;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

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

	private static final List<String> BLACKLISTED_EMAILS = List.of("tripadvisor", "roses", "puzzles", "youtube",
		"messages.telstra.com", "storm", "marvel", "paypal", "tennis", "mightymunch");

	private final AwsSecretsService awsSecretsService;
	private final RefDataService refDataService;
	private final NotificationService notificationService;

	@Value("${email.secret.name:email-credentials}")
	private String emailSecretName;

	@Autowired
	public EmailTrawler(AwsSecretsService awsSecretsService, RefDataService refDataService,
		NotificationService notificationService) {
		this.awsSecretsService = awsSecretsService;
		this.refDataService = refDataService;
		this.notificationService = notificationService;
	}

	public void check() {
		Store store = null;
		Folder emailFolder = null;

		try {
			String user = awsSecretsService.getSecretValue(emailSecretName, "USER_NAME");
			String password = awsSecretsService.getSecretValue(emailSecretName, "PASSWORD");

			Session emailSession = createEmailSession();
			store = emailSession.getStore(MAIL_PROTOCOL);
			store.connect(GMAIL_HOST, user, password);

			emailFolder = store.getFolder(INBOX_FOLDER);
			emailFolder.open(Folder.READ_WRITE);

			Message[] messages = fetchUnreadMessages(emailFolder);
			if (messages.length == 0) {
				LOGGER.info("No unread messages found");
				return;
			}

			LOGGER.info("Processing {} unread messages", messages.length);
			List<RefData> refDatas = refDataService.getAllWithEmailKey();

			processMessages(messages, refDatas);

		} catch (Exception e) {
			LOGGER.error("Error checking emails", e);
		} finally {
			closeResources(emailFolder, store);
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

	private void processMessages(Message[] messages, List<RefData> refDatas) {
		for (Message message : messages) {
			try {
				if (isBlacklisted(message)) {
					LOGGER.debug("Skipping blacklisted email from: {}", getFromAddress(message));
					markAsRead(message);
					continue;
				}

				String subject = message.getSubject();
				LOGGER.info("Processing email: {}", subject);

				boolean processed = processMessage(message, refDatas);

				if (!processed) {
					createUnhandledNotification(subject);
				}

				markAsRead(message);

			} catch (Exception e) {
				LOGGER.error("Error processing message", e);
			}
		}
	}

	private boolean processMessage(Message message, List<RefData> refDatas) throws MessagingException, IOException {
		for (RefData refData : refDatas) {
			if (refDataMatch(message, refData)) {
				LOGGER.info("Matched processor: {}",
					refData.getEmailProcessor().getProcessor().getClass().getSimpleName());
				try {
					refData.getEmailProcessor().getProcessor().execute(message, refData);
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

	private void closeResources(Folder folder, Store store) {
		try {
			if (folder != null && folder.isOpen()) {
				folder.close(false);
			}
		} catch (MessagingException e) {
			LOGGER.warn("Error closing folder", e);
		}

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

	private boolean refDataMatch(Message message, RefData refData) throws MessagingException, IOException {
		String subject = message.getSubject();
		if (subject == null) {
			return false;
		}

		String emailKey = refData.getEmailKey();
		EmailProcessor processor = refData.getEmailProcessor();

		// Check for RACV-specific processing
		if (bodyContains(message, "RACV")) {
			return matchRACVEmail(message, subject, emailKey, processor);
		}

		// Default: simple subject match
		return subject.contains(emailKey);
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
