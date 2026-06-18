package au.com.mason.expensemanager.pdf.rental;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RentalStatementData(
		BigDecimal totalRent,
		BigDecimal managementFee,
		BigDecimal adminFee,
		LocalDate statementFrom,
		LocalDate statementTo,
		BigDecimal paymentToOwner) {

	public BigDecimal expectedPaymentToOwner() {
		return totalRent.subtract(managementFee).subtract(adminFee);
	}

	public boolean isBalanced() {
		return paymentToOwner.compareTo(expectedPaymentToOwner()) == 0;
	}

}
