package au.com.mason.expensemanager.dto;

public class ErrorDto {

	private String errorName;
	private String errorMessage;

	public ErrorDto(String errorName, String errorMessage) {
		this.errorName = errorName;
		this.errorMessage = errorMessage;
	}

	public String getErrorName() {
		return errorName;
	}

	public void setErrorName(String errorName) {
		this.errorName = errorName;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
