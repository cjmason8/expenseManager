package au.com.mason.expensemanager.mail;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

final class EmailMessagePartsTestSupport {

	static MimeMessage multipartMessage(String html, byte[] pdfBytes, String pdfContentType) throws Exception {
		Session session = Session.getDefaultInstance(new Properties());
		MimeMessage message = new MimeMessage(session);

		MimeMultipart multipart = new MimeMultipart();
		if (html != null) {
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(html, "text/html");
			multipart.addBodyPart(htmlPart);
		}
		if (pdfBytes != null) {
			MimeBodyPart pdfPart = new MimeBodyPart();
			pdfPart.setDataHandler(new DataHandler(new DataSource() {
				@Override
				public java.io.InputStream getInputStream() {
					return new ByteArrayInputStream(pdfBytes);
				}

				@Override
				public java.io.OutputStream getOutputStream() {
					throw new UnsupportedOperationException();
				}

				@Override
				public String getContentType() {
					return pdfContentType;
				}

				@Override
				public String getName() {
					return "attachment.pdf";
				}
			}));
			multipart.addBodyPart(pdfPart);
		}

		message.setContent(multipart);
		message.saveChanges();
		return message;
	}

	static MimeMessage nestedMultipartMessage(String innerHtml) throws Exception {
		Session session = Session.getDefaultInstance(new Properties());
		MimeMessage message = new MimeMessage(session);

		MimeMultipart outer = new MimeMultipart();
		MimeBodyPart container = new MimeBodyPart();

		MimeMultipart inner = new MimeMultipart();
		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(innerHtml, "text/html");
		inner.addBodyPart(htmlPart);
		container.setContent(inner);
		outer.addBodyPart(container);

		message.setContent(outer);
		message.saveChanges();
		return message;
	}

	private EmailMessagePartsTestSupport() {
	}

}
