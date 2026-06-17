package au.com.mason.expensemanager.domain;

import java.time.temporal.ChronoUnit;

public enum RecurringUnit {
	WEEKLY(7, ChronoUnit.DAYS), FORTNIGHTLY(14, ChronoUnit.DAYS), MONTHLY(1, ChronoUnit.MONTHS), YEARLY(1,
		ChronoUnit.YEARS), QUARTERLY(3, ChronoUnit.MONTHS), FOUR_WEEKLY(4, ChronoUnit.WEEKS), EVERY_TWO_MONTHS(2,
			ChronoUnit.MONTHS), EVERY_SIX_MONTHS(6,
				ChronoUnit.MONTHS), EVERY_FOUR_MONTHS(4, ChronoUnit.MONTHS), BI_MONTHLY(2, ChronoUnit.WEEKS);

	private Integer units;
	private ChronoUnit unitType;

	private RecurringUnit(Integer units, ChronoUnit unitType) {
		this.units = units;
		this.unitType = unitType;
	}

	public String getValue() {
		return this.name();
	}

	public Integer getUnits() {
		return units;
	}

	public ChronoUnit getUnitType() {
		return unitType;
	}
}
