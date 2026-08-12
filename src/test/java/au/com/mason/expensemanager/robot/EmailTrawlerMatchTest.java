package au.com.mason.expensemanager.robot;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import au.com.mason.expensemanager.service.AwsSecretsService;
import au.com.mason.expensemanager.service.NotificationService;
import au.com.mason.expensemanager.service.RefDataService;

@ExtendWith(MockitoExtension.class)
class EmailTrawlerMatchTest {

	@Mock
	private AwsSecretsService awsSecretsService;

	@Mock
	private RefDataService refDataService;

	@Mock
	private NotificationService notificationService;

	private EmailTrawler emailTrawler;

	@BeforeEach
	void setUp() {
		emailTrawler = new EmailTrawler(awsSecretsService, refDataService, notificationService);
	}

	@Test
	void matchesEmailKey_matchesHobsonsBayInFromWhenSubjectIsGenericRatesNotice() throws Exception {
		MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
		message.setFrom(new InternetAddress("noreply@enotices.com.au", "Hobsons Bay City Council"));
		message.setSubject("Your Rates Notice");
		message.setText("Rates notice");
		message.saveChanges();

		assertTrue(emailTrawler.matchesEmailKey(message, "Your Rates Notice", "Hobsons Bay"));
		assertFalse(emailTrawler.matchesEmailKey(message, "Your Rates Notice", "City of Wodonga"));
	}

	@Test
	void matchesEmailKey_matchesSubject() throws Exception {
		MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
		message.setFrom(new InternetAddress("bills@example.com"));
		message.setSubject("Greater Western Water bill available");
		message.setText("bill");
		message.saveChanges();

		assertTrue(emailTrawler.matchesEmailKey(message, message.getSubject(), "Greater Western Water bill"));
	}

	@Test
	void matchesEmailKey_matchesNestedBody() throws Exception {
		MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
		message.setFrom(new InternetAddress("noreply@example.com"));
		message.setSubject("Your Rates Notice");

		MimeMultipart mixed = new MimeMultipart("mixed");
		MimeBodyPart altContainer = new MimeBodyPart();
		MimeMultipart alternative = new MimeMultipart("alternative");
		MimeBodyPart html = new MimeBodyPart();
		html.setContent("<html>City of Kingston rates</html>", "text/html");
		alternative.addBodyPart(html);
		altContainer.setContent(alternative);
		mixed.addBodyPart(altContainer);
		message.setContent(mixed);
		message.saveChanges();

		assertTrue(emailTrawler.matchesEmailKey(message, "Your Rates Notice", "City of Kingston"));
	}

	@Test
	void matchesEmailKey_blankKeyDoesNotMatch() throws Exception {
		MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
		message.setSubject("Anything");
		message.setText("x");
		message.saveChanges();

		assertFalse(emailTrawler.matchesEmailKey(message, "Anything", " "));
		assertFalse(emailTrawler.matchesEmailKey(message, "Anything", null));
	}

}
