package au.com.mason.expensemanager.mail;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;

import org.junit.jupiter.api.Test;

class EmailMessagePartsTest {

	@Test
	void firstHtml_extractsHtmlFromMultipartMessage() throws Exception {
		var message = EmailMessagePartsTestSupport.multipartMessage("<html>bill</html>", null, null);

		assertEquals("<html>bill</html>", EmailMessageParts.firstHtml(message).orElseThrow());
	}

	@Test
	void htmlBody_extractsFromSinglePartHtmlMessage() throws Exception {
		SessionHolder message = new SessionHolder();
		message.setContent("<html>water</html>", "text/html");

		assertEquals("<html>water</html>", EmailMessageParts.htmlBody(message.message()).orElseThrow());
	}

	@Test
	void firstMatchingAttachment_findsPdfPart() throws Exception {
		byte[] pdfBytes = new byte[]{1, 2, 3};
		var message = EmailMessagePartsTestSupport.multipartMessage("<html/>", pdfBytes, "APPLICATION/PDF");

		assertArrayEquals(pdfBytes, EmailMessageParts.firstMatchingAttachment(message, part -> {
			try {
				return EmailMessageParts.isPdfPart(part);
			} catch (jakarta.mail.MessagingException e) {
				throw new RuntimeException(e);
			}
		}).orElseThrow());
	}

	@Test
	void lastNonHtmlAttachment_returnsFinalAttachment() throws Exception {
		byte[] firstPdf = new byte[]{1};
		byte[] lastPdf = new byte[]{2, 3};
		var message = EmailMessagePartsTestSupport.multipartMessage("<html/>", firstPdf, "APPLICATION/PDF");
		MimeMultipart multipart = (MimeMultipart) message.getContent();
		MimeBodyPart secondPdf = new MimeBodyPart();
		secondPdf.setContent(lastPdf, "application/pdf");
		multipart.addBodyPart(secondPdf);

		assertArrayEquals(lastPdf, EmailMessageParts.lastNonHtmlAttachment(message).orElseThrow());
	}

	@Test
	void allParts_flattensNestedMultipart() throws Exception {
		var message = EmailMessagePartsTestSupport.nestedMultipartMessage("<html>nested</html>");

		assertEquals(1, EmailMessageParts.allParts(message).size());
		assertTrue(EmailMessageParts.isHtmlPart(EmailMessageParts.allParts(message).get(0)));
	}

	private static final class SessionHolder {
		private final jakarta.mail.internet.MimeMessage message;

		private SessionHolder() throws Exception {
			message = new jakarta.mail.internet.MimeMessage(
				jakarta.mail.Session.getDefaultInstance(new java.util.Properties()));
		}

		private void setContent(String content, String type) throws Exception {
			message.setContent(content, type);
			message.saveChanges();
		}

		private jakarta.mail.internet.MimeMessage message() {
			return message;
		}
	}

}
