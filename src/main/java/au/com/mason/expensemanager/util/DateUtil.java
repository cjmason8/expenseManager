package au.com.mason.expensemanager.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

import au.com.mason.expensemanager.domain.RecurringUnit;
import au.com.mason.expensemanager.domain.Transaction;

public class DateUtil {
	
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public static LocalDate getMonday(LocalDate date) {
		return date.with(DayOfWeek.MONDAY);
	}
	
	public static LocalDate getFormattedDate(String date) {
		if (date.indexOf("Z") != -1) {
			ZonedDateTime createdAtUTC = ZonedDateTime.parse(date);
			ZonedDateTime createdAtMelb = createdAtUTC.withZoneSameInstant(ZoneId.of("Australia/Melbourne"));
			
			return createdAtMelb.toLocalDate();
		}
		else {
			return LocalDate.parse(date, FORMATTER);
		}
	}
	
	public static String getFormattedDbDate(LocalDate date) {
		return DB_FORMATTER.format(date);
	}
	
	public static String getFormattedDbDate(String date) {
		if (date == null || date.isBlank()) {
			return null;
		}
		return getFormattedDbDate(getFormattedDate(date));
	}
	
	public static String getFormattedDateString(LocalDate date) {
		return FORMATTER.format(date);
	}
	
	public static LocalDate findDueDate(Transaction transaction, LocalDate dueDate) {
		RecurringUnit recurringUnit = 
				RecurringUnit.valueOf(transaction.getRecurringType().getDescriptionUpper());
		
		if (recurringUnit.equals(RecurringUnit.BI_MONTHLY)) {
			if (dueDate.getDayOfMonth() == 15) {
				dueDate = dueDate.with(TemporalAdjusters.lastDayOfMonth());
			}
			else {
				dueDate = dueDate.plus(1, ChronoUnit.MONTHS).withDayOfMonth(15);
			}
		}
		else {
			dueDate = dueDate.plus(recurringUnit.getUnits(), recurringUnit.getUnitType());
		}
		
		return dueDate;
	}

}
