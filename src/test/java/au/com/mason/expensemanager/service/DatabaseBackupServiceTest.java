package au.com.mason.expensemanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class DatabaseBackupServiceTest {

	@Test
	void dailyKey_usesDateStamp() {
		assertEquals("daily/expensemanager-2026-08-27.sql",
			DatabaseBackupService.dailyKey(LocalDate.of(2026, 8, 27)));
	}

	@Test
	void weeklyKey_usesSundayDateStamp() {
		assertEquals("weekly/expensemanager-week-2026-08-31.zip",
			DatabaseBackupService.weeklyKey(LocalDate.of(2026, 8, 31)));
	}

	@Test
	void selectWeeklyKeysToDelete_keepsNewestTwo() {
		List<String> keys = List.of(
			"weekly/expensemanager-week-2026-08-17.zip",
			"weekly/expensemanager-week-2026-08-24.zip",
			"weekly/expensemanager-week-2026-08-31.zip");

		List<String> toDelete = DatabaseBackupService.selectWeeklyKeysToDelete(keys, 2);

		assertEquals(1, toDelete.size());
		assertEquals("weekly/expensemanager-week-2026-08-17.zip", toDelete.get(0));
	}

	@Test
	void selectWeeklyKeysToDelete_returnsEmptyWhenWithinRetention() {
		List<String> keys = List.of(
			"weekly/expensemanager-week-2026-08-24.zip",
			"weekly/expensemanager-week-2026-08-31.zip");

		assertTrue(DatabaseBackupService.selectWeeklyKeysToDelete(keys, 2).isEmpty());
	}

}
