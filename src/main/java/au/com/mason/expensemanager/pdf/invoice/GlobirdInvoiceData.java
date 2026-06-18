package au.com.mason.expensemanager.pdf.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GlobirdInvoiceData(LocalDate issueDate, LocalDate dueDate, String amount, boolean zeroCredit) {

	public static GlobirdInvoiceData zeroCredit(LocalDate issueDate) {
		return new GlobirdInvoiceData(issueDate, null, BigDecimal.ZERO.toString(), true);
	}

}
