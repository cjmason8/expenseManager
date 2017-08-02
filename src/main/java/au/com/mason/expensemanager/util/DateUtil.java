package au.com.mason.expensemanager.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

import org.aspectj.weaver.patterns.WithinAnnotationPointcut;

import au.com.mason.expensemanager.domain.RecurringUnit;
import au.com.mason.expensemanager.domain.Transaction;

public class DateUtil {
	
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

	public static LocalDate getMonday(LocalDate date) {
		return date.with(DayOfWeek.MONDAY);
	}
	
	public static LocalDate getFormattedDate(String date) {
		return LocalDate.parse(date, FORMATTER);
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
