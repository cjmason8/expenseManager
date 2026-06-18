package au.com.mason.expensemanager.pdf.rates;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RatesInstalmentData(
		LocalDate dueDate,
		String amount,
		int instalmentNumber) {

	public String notes() {
		return switch (instalmentNumber) {
			case 1 -> "1st Instalment";
			case 2 -> "2nd Instalment";
			case 3 -> "3rd Instalment";
			case 4 -> "4th Instalment";
			default -> "";
		};
	}

	public BigDecimal amountAsBigDecimal() {
		return new BigDecimal(amount.replace("$", "").replace(",", ""));
	}

}
