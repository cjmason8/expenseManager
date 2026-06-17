package au.com.mason.expensemanager.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import au.com.mason.expensemanager.domain.RentalPayment;

class RentalPaymentFinancialYearTest {

	@Test
	void financialYearEnd_usesStatementToForSouthKingsville() {
		RentalPayment payment = new RentalPayment();
		payment.setProperty("STH_KINGSVILLE");
		payment.setStatementFrom(LocalDate.of(2026, 6, 10));
		payment.setStatementTo(LocalDate.of(2026, 7, 10));

		assertEquals(2027, RentalPaymentFinancialYear.financialYearEnd(payment));
	}

	@Test
	void financialYearEnd_usesStatementFromForWodonga() {
		RentalPayment payment = new RentalPayment();
		payment.setProperty("WODONGA");
		payment.setStatementFrom(LocalDate.of(2025, 7, 1));
		payment.setStatementTo(LocalDate.of(2025, 7, 31));

		assertEquals(2026, RentalPaymentFinancialYear.financialYearEnd(payment));
	}

	@Test
	void defaultFinancialYearEnd_inJuneUsesUpcomingFinancialYear() {
		assertEquals(2027, RentalPaymentFinancialYear.defaultFinancialYearEnd(LocalDate.of(2026, 6, 11)));
	}

	@Test
	void defaultFinancialYearEnd_beforeJuneUsesCurrentFinancialYear() {
		assertEquals(2026, RentalPaymentFinancialYear.defaultFinancialYearEnd(LocalDate.of(2026, 5, 15)));
	}

}
