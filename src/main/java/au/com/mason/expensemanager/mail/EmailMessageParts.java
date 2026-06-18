package au.com.mason.expensemanager.mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;
import org.eclipse.angus.mail.util.BASE64DecoderStream;

public final class EmailMessageParts {

	private EmailMessageParts() {
	}

	public static boolean isMultipart(Message message) throws MessagingException {
		return message.isMimeType("multipart/*") || message.getContentType().toUpperCase().startsWith("MULTIPART/");
	}

	public static boolean isPlainText(Message message) throws MessagingException {
		return message.isMimeType("text/plain");
	}

	public static boolean isHtml(Message message) throws MessagingException {
		return message.isMimeType("text/html") || message.getContentType().toUpperCase().startsWith("TEXT/HTML");
	}

	public static Optional<String> htmlBody(Message message) throws MessagingException, IOException {
		if (isHtml(message)) {
			return Optional.of((String) message.getContent());
		}
		return firstHtml(message);
	}

	public static List<BodyPart> allParts(Message message) throws MessagingException, IOException {
		if (!isMultipart(message)) {
			return List.of();
		}
		return flattenMultipart((MimeMultipart) message.getContent());
	}

	public static boolean isHtmlPart(BodyPart bodyPart) throws MessagingException {
		return bodyPart.isMimeType("text/html") || bodyPart.getContentType().toUpperCase().startsWith("TEXT/HTML");
	}

	public static boolean isPdfPart(BodyPart bodyPart) throws MessagingException {
		return bodyPart.isMimeType("application/pdf")
			|| bodyPart.getContentType().toUpperCase().startsWith("APPLICATION/PDF");
	}

	public static boolean isOctetStreamPart(BodyPart bodyPart) throws MessagingException {
		return bodyPart.isMimeType("application/octet-stream")
			|| bodyPart.getContentType().toUpperCase().startsWith("APPLICATION/OCTET-STREAM");
	}

	public static boolean isNonHtmlPart(BodyPart bodyPart) throws MessagingException {
		return !isHtmlPart(bodyPart);
	}

	public static String readHtml(BodyPart bodyPart) throws MessagingException, IOException {
		return bodyPart.getContent().toString();
	}

	public static byte[] readBytes(BodyPart bodyPart) throws MessagingException, IOException {
		Object content = bodyPart.getContent();
		if (content instanceof BASE64DecoderStream base64Stream) {
			return IOUtils.toByteArray(base64Stream);
		}
		if (content instanceof InputStream inputStream) {
			return IOUtils.toByteArray(inputStream);
		}
		if (content instanceof byte[] bytes) {
			return bytes;
		}
		throw new IllegalStateException("Unsupported attachment content type: " + content.getClass().getName());
	}

	public static Optional<String> firstHtml(Message message) throws MessagingException, IOException {
		for (BodyPart bodyPart : allParts(message)) {
			if (isHtmlPart(bodyPart)) {
				return Optional.of(readHtml(bodyPart));
			}
		}
		return Optional.empty();
	}

	public static Optional<byte[]> firstMatchingAttachment(Message message, Predicate<BodyPart> matcher)
		throws MessagingException, IOException {
		for (BodyPart bodyPart : allParts(message)) {
			if (matcher.test(bodyPart)) {
				return Optional.of(readBytes(bodyPart));
			}
		}
		return Optional.empty();
	}

	public static Optional<byte[]> lastNonHtmlAttachment(Message message) throws MessagingException, IOException {
		byte[] lastAttachment = null;
		for (BodyPart bodyPart : allParts(message)) {
			if (isNonHtmlPart(bodyPart)) {
				lastAttachment = readBytes(bodyPart);
			}
		}
		return Optional.ofNullable(lastAttachment);
	}

	private static List<BodyPart> flattenMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
		List<BodyPart> parts = new ArrayList<>();
		for (int i = 0; i < mimeMultipart.getCount(); i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (isMultipartPart(bodyPart)) {
				parts.addAll(flattenMultipart((MimeMultipart) bodyPart.getContent()));
			} else {
				parts.add(bodyPart);
			}
		}
		return parts;
	}

	private static boolean isMultipartPart(BodyPart bodyPart) throws MessagingException {
		return bodyPart.isMimeType("multipart/*") || bodyPart.getContentType().toUpperCase().startsWith("MULTIPART/");
	}

}
