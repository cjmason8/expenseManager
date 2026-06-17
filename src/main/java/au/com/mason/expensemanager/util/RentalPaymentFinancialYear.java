package au.com.mason.expensemanager.util;

import java.time.LocalDate;

import au.com.mason.expensemanager.domain.RentalPayment;

public final class RentalPaymentFinancialYear {

	private RentalPaymentFinancialYear() {
	}

	public static int financialYearEnd(RentalPayment payment) {
		LocalDate referenceDate = referenceDate(payment);
		if (referenceDate == null) {
			return 0;
		}
		return financialYearEnd(referenceDate);
	}

	public static int financialYearEnd(LocalDate referenceDate) {
		return referenceDate.getMonthValue() <= 6 ? referenceDate.getYear() : referenceDate.getYear() + 1;
	}

	public static int defaultFinancialYearEnd(LocalDate today) {
		if (today.getMonthValue() == 6) {
			return today.getYear() + 1;
		}
		return today.getMonthValue() <= 6 ? today.getYear() : today.getYear() + 1;
	}

	private static LocalDate referenceDate(RentalPayment payment) {
		if ("STH_KINGSVILLE".equals(payment.getProperty())) {
			return payment.getStatementTo();
		}
		return payment.getStatementFrom();
	}

}
