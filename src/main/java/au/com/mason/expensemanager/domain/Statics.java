package au.com.mason.expensemanager.domain;

public enum Statics {

	MAX_RESULTS("20");

	private String value;

	private Statics(String value) {
		this.value = value;
	}

	public String getStringValue() {
		return value;
	}

	public Integer getIntValue() {
		return Integer.valueOf(value);
	}

}
