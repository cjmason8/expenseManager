package au.com.mason.expensemanager.domain;

public enum RefDataType {

	CAUSE("Cause"), EXPENSE_TYPE("Expense Type"), INCOME_TYPE("Income Type"), RECURRING_TYPE("Recurring Type");

	private String description;

	private RefDataType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
