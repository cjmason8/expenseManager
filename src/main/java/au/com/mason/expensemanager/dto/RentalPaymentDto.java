package au.com.mason.expensemanager.dto;

public class RentalPaymentDto {

	private Long id;
	private String property;
	private String statementFromString;
	private String statementToString;
	private String totalRent;
	private String managementFee;
	private String adminFee;
	private String otherFee;
	private DocumentDto documentDto;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getStatementFromString() {
		return statementFromString;
	}

	public void setStatementFromString(String statementFromString) {
		this.statementFromString = statementFromString;
	}

	public String getStatementToString() {
		return statementToString;
	}

	public void setStatementToString(String statementToString) {
		this.statementToString = statementToString;
	}

	public String getTotalRent() {
		return totalRent;
	}

	public void setTotalRent(String totalRent) {
		this.totalRent = totalRent;
	}

	public String getManagementFee() {
		return managementFee;
	}

	public void setManagementFee(String managementFee) {
		this.managementFee = managementFee;
	}

	public String getAdminFee() {
		return adminFee;
	}

	public void setAdminFee(String adminFee) {
		this.adminFee = adminFee;
	}

	public String getOtherFee() {
		return otherFee;
	}

	public void setOtherFee(String otherFee) {
		this.otherFee = otherFee;
	}

	public DocumentDto getDocumentDto() {
		return documentDto;
	}

	public void setDocumentDto(DocumentDto documentDto) {
		this.documentDto = documentDto;
	}
	
}
