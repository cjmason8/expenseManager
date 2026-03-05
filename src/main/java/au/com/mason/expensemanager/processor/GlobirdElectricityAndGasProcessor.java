package au.com.mason.expensemanager.processor;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.pdf.PdfReader;
import au.com.mason.expensemanager.util.CollectionUtil;
import com.sun.mail.util.BASE64DecoderStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class GlobirdElectricityAndGasProcessor extends Processor {

	private static Logger LOGGER = LogManager.getLogger(GlobirdElectricityAndGasProcessor.class);

	@Override
	public void execute(Message message, RefData refData) throws Exception {
		if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			int count = mimeMultipart.getCount();
			LocalDate dueDate = null;
			String amount = null;
			Document document = null;
			boolean foundAmountDue = false;
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = mimeMultipart.getBodyPart(i);
				if (bodyPart.getContentType().startsWith("APPLICATION/PDF") && bodyPart.getFileName().startsWith("Invoice")) {
					BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getContent();
					byte[] byteArray = IOUtils.toByteArray(base64DecoderStream);

					String content = PdfReader.extract(byteArray);
					List<String> lines = CollectionUtil.splitAndConvert(content, "\n");
					for (String line : lines) {
						if (line.startsWith("Due Date")) {
							dueDate = LocalDate.parse(line.substring(line.lastIndexOf(" ") + 1), DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
						}
						else if (line.startsWith("Amount Due")) {
							foundAmountDue = true;
						}
						else if (foundAmountDue) {
							amount = line.substring(1);
							break;
						}
					}

					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
					String fileName = "Globird - " + formatter.format(dueDate) + ".pdf";
					document = documentService.createDocumentFromEmailForExpense(byteArray, fileName);
				}
			}

			LOGGER.info("Adding a Globird expense - dueDate - " + dueDate + ", amount - " + amount);
			updateExpense(refData, dueDate, amount, document);
		}
	}

}
