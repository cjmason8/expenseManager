package au.com.mason.expensemanager.processor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.mail.EmailMessageParts;
import au.com.mason.expensemanager.pdf.invoice.GlobirdInvoiceData;
import au.com.mason.expensemanager.pdf.invoice.GlobirdInvoicePdfParser;

@Component
public class GlobirdElectricityAndGasProcessor extends Processor {

	private static final Logger LOGGER = LogManager.getLogger(GlobirdElectricityAndGasProcessor.class);

	@Autowired
	private GlobirdInvoicePdfParser globirdInvoicePdfParser;

	@Override
	public void execute(Message message, RefData refData) throws Exception {
		if (!EmailMessageParts.isMultipart(message)) {
			return;
		}

		LocalDate dueDate = null;
		String amount = null;
		Document document = null;
		String notes = null;

		for (BodyPart bodyPart : EmailMessageParts.allParts(message)) {
			if (!EmailMessageParts.isPdfPart(bodyPart) || !bodyPart.getFileName().startsWith("Invoice")) {
				continue;
			}

			byte[] pdfBytes = EmailMessageParts.readBytes(bodyPart);
			GlobirdInvoiceData invoice = globirdInvoicePdfParser.parse(pdfBytes);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");

			if (invoice.zeroCredit()) {
				dueDate = invoice.issueDate().plusMonths(1).withDayOfMonth(10);
				amount = BigDecimal.ZERO.toString();
				notes = "Amount due is zero due to credit, therefore due date is fake.";
				document = documentService.createDocumentFromEmailForExpense(pdfBytes,
					"Globird - " + formatter.format(invoice.issueDate()) + ".pdf");
			} else {
				dueDate = invoice.dueDate();
				amount = invoice.amount();
				document = documentService.createDocumentFromEmailForExpense(pdfBytes,
					"Globird - " + formatter.format(invoice.dueDate()) + ".pdf");
			}
		}

		LOGGER.info("Adding a Globird expense - dueDate - {}, amount - {}", dueDate, amount);
		updateExpense(refData, dueDate, amount, document, notes);
	}

}
