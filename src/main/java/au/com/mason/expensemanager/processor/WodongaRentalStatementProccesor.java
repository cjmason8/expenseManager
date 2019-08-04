package au.com.mason.expensemanager.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import com.sun.mail.util.BASE64DecoderStream;

import au.com.mason.expensemanager.domain.RefData;

@Component
public class WodongaRentalStatementProccesor extends Processor {

	@Override
	public void execute(Message message, RefData refData) throws Exception {
		List<String> firstSixMonths = new ArrayList<>();
		firstSixMonths.add("Jan");
		firstSixMonths.add("Feb");
		firstSixMonths.add("Mar");
		firstSixMonths.add("Apr");
		firstSixMonths.add("May");
		firstSixMonths.add("Jun");
		if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			int count = mimeMultipart.getCount();
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = mimeMultipart.getBodyPart(i);
				if (bodyPart.isMimeType("text/html")) {
				} else if (bodyPart.getContentType().startsWith("APPLICATION/OCTET-STREAM")) {
					BASE64DecoderStream base64DecoderStream = (BASE64DecoderStream) bodyPart.getContent();
					byte[] byteArray = IOUtils.toByteArray(base64DecoderStream);
					String fileName = message.getSubject().substring(0, message.getSubject().indexOf("(") - 1) + ".pdf";
					String folder = fileName.substring(fileName.indexOf(" to ") + 4).replace(".pdf", "");
					String month = folder.substring(folder.indexOf(" "), folder.lastIndexOf(" "));
					String year = folder.substring(folder.lastIndexOf(" ") + 1);
					if (firstSixMonths.contains(month)) {
						folder = (Integer.valueOf(year) - 1) + "-" + year; 
					}
					else {
						folder = year + "-" + (Integer.valueOf(year) + 1);
					}
					Map<String, Object> metaData = new HashMap<>();
					metaData.put("property", "Wodonga");
					metaData.put("year", folder);
					documentService.createDocumentForRentalStatement(byteArray, fileName,
							"/Wodonga/" + folder + "/Statements", metaData);
				}
			}
		}
	}

}
