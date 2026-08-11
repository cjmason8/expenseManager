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
		return multipartMessage(html, pdfBytes, pdfContentType, true);
	}

	static MimeMessage multipartMessage(String html, byte[] pdfBytes, String pdfContentType, boolean htmlFirst)
		throws Exception {
		Session session = Session.getDefaultInstance(new Properties());
		MimeMessage message = new MimeMessage(session);

		MimeMultipart multipart = new MimeMultipart();
		MimeBodyPart htmlPart = null;
		if (html != null) {
			htmlPart = new MimeBodyPart();
			htmlPart.setContent(html, "text/html");
		}
		MimeBodyPart pdfPart = null;
		if (pdfBytes != null) {
			pdfPart = buildPdfPart(pdfBytes, pdfContentType);
		}

		if (htmlFirst) {
			if (htmlPart != null) {
				multipart.addBodyPart(htmlPart);
			}
			if (pdfPart != null) {
				multipart.addBodyPart(pdfPart);
			}
		} else {
			if (pdfPart != null) {
				multipart.addBodyPart(pdfPart);
			}
			if (htmlPart != null) {
				multipart.addBodyPart(htmlPart);
			}
		}

		message.setContent(multipart);
		message.saveChanges();
		return message;
	}

	private static MimeBodyPart buildPdfPart(byte[] pdfBytes, String pdfContentType) throws Exception {
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
		return pdfPart;
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
