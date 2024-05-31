package au.com.mason.expensemanager.robot;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Notification;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.processor.EmailProcessor;
import au.com.mason.expensemanager.service.EncryptionService;
import au.com.mason.expensemanager.service.NotificationService;
import au.com.mason.expensemanager.service.RefDataService;

@Component
public class EmailTrawler {
	
	private static final Logger LOGGER = LogManager.getLogger(EmailTrawler.class);
	
	@Autowired
	private EncryptionService encryptionService;
	
	@Autowired
	private RefDataService refDataService;
	
	@Autowired
	protected NotificationService notificationService;
	
	@Value("${required.info}")
	private String requiredKey;
	
	@Value("${req.account}")
	private String reqAccount;
	
	public void check() {
		try {
			String host = "pop.gmail.com";// change accordingly
			String user = encryptionService.decrypt(reqAccount);
			String password = encryptionService.decrypt(requiredKey);
			
			// create properties field
			Properties properties = new Properties();
			properties.put("mail.store.protocol", "imaps");
			properties.put("mail.imaps.ssl.trust", host);
			properties.put("mail.imaps.ssl.protocols", "TLSv1.2");

			Session emailSession = Session.getDefaultInstance(properties);
			Store store = emailSession.getStore();
			store.connect(host, user, password);
			
			Folder emailFolder = store.getFolder("INBOX");
			// use READ_ONLY if you don't wish the messages
			// to be marked as read after retrieving its content
			emailFolder.open(Folder.READ_WRITE);

			Message[] messages = emailFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
			System.out.println("messages.length---" + messages.length);
			
			List<RefData> refDatas = refDataService.getAllWithEmailKey(); 

			for (Message message : messages) {
				if (Arrays.stream(message.getFrom()).filter(f -> matchEmail(f.toString())).count() > 0) {
					markAsRead(message);
					continue;
				}

				System.out.println("Handling Subject: " + message.getSubject());
				boolean foundIt = false;
				for (RefData refData : refDatas) {
					if (refDataMatch(message, refData)) {
						System.out.println("Found Processor: " + refData.getEmailProcessor().getProcessor().getClass());
						refData.getEmailProcessor().getProcessor().execute(message, refData);
						foundIt = true;
						break;
					}
				}
				
				if (!foundIt) {
					Notification notification = new Notification();
					notification.setMessage("Unhandled Email with title - " + message.getSubject());
					notificationService.create(notification);
				}
				
				//mark as read
				markAsRead(message);
			}

			// close the store and folder objects
			emailFolder.close(false);
			store.close();

		} catch (Exception e) {
			LOGGER.error(e);
			e.printStackTrace();
		}
	}

	private static void markAsRead(Message message) throws IOException, MessagingException {
		message.getContent();
		MimeMessage source = (MimeMessage) message;
		MimeMessage copy = new MimeMessage(source);
	}

	private boolean matchEmail(String email) {
		List<String> blackListedEmails = List.of("tripadvisor", "roses", "puzzles", "youtube", "messages.telstra.com", "storm", "marvel", "paypal", "tennis", "mightymunch");

		return blackListedEmails.stream().anyMatch(email::contains);
	}

	private boolean refDataMatch(Message message, RefData refData) throws MessagingException, IOException {
		if (bodyContains(message, "RACV")) {
			if (refData.getEmailProcessor().equals(EmailProcessor.RACV_MEMBERSHIP)) {
				String fromAddress = ((InternetAddress) message.getFrom()[0]).getAddress();
				
				return message.getSubject().startsWith(refData.getEmailKey()) && fromAddress.startsWith("racvrenewal_noreply");
			}
			else if (refData.getEmailKey().equals("Your Renewal RACV Comprehensive")) {
				if (refData.getEmailProcessor().equals(EmailProcessor.CAMRY_INSURANCE)) {
					return bodyContains(message, "TOYOTA CAMRY");
				}
				else if (refData.getEmailProcessor().equals(EmailProcessor.FORD_INSURANCE)) {
					return bodyContains(message, "FORD FAIRMONT");
				}
				else {
					return bodyContains(message, "MAZDA TRIBUTE");
				}
			}
			else if (refData.getEmailKey().equals("Your Renewal RACV Home Buildings Ins")) {
				if (refData.getEmailProcessor().equals(EmailProcessor.WODONGA_INSURANCE)) {
					return bodyContains(message, "WODONGA");
				}
				else {
					return bodyContains(message, "SOUTH KINGSVILLE");
				}
			}
			else if (refData.getEmailProcessor().equals(EmailProcessor.DINGLEY_INSURANCE)) {
				return message.getSubject().startsWith(refData.getEmailKey());
			}
			
			return false;
		}

		return message.getSubject().indexOf(refData.getEmailKey()) != -1;
	}
	
	private boolean bodyContains(Message message, String phrase) throws MessagingException, IOException {
		if (message.isMimeType("multipart/*")) {
	        MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
	        int count = mimeMultipart.getCount();
		    for (int i = 0; i < count; i++) {
		        BodyPart bodyPart = mimeMultipart.getBodyPart(i);
		        if (bodyPart.isMimeType("text/html")) {
		            return ((String) bodyPart.getContent()).contains(phrase);
		        }
		    }
	    }
		
		return false;
	}
	
	public Message[] fetchMessages(String host, String user, String password, boolean read) throws Exception {
		Properties properties = new Properties();
		properties.put("mail.store.protocol", "imaps");

		Session emailSession = Session.getDefaultInstance(properties);
		Store store = emailSession.getStore();
		store.connect(host, user, password);

		Folder emailFolder = store.getFolder("INBOX");
		// use READ_ONLY if you don't wish the messages
		// to be marked as read after retrieving its content
		emailFolder.open(Folder.READ_WRITE);

		// search for all "unseen" messages
		Flags seen = new Flags(Flags.Flag.SEEN);
		FlagTerm unseenFlagTerm = new FlagTerm(seen, read);
		return emailFolder.search(unseenFlagTerm);
	}

}
