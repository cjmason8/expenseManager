package au.com.mason.expensemanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class EntityEntryServiceTest {

	@Test
	void formatLeaveDays_convertsYtdHoursToWholeDays() {
		assertEquals("16 days", EntityEntryService.formatLeaveDays(new BigDecimal("128")));
	}

	@Test
	void formatLeaveDays_keepsFractionalDays() {
		assertEquals("16.25 days", EntityEntryService.formatLeaveDays(new BigDecimal("130")));
	}

}
