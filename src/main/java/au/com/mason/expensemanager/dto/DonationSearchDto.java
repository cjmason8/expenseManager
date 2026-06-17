package au.com.mason.expensemanager.dto;

public class DonationSearchDto {

	private RefDataDto cause;
	private String startDate;
	private String endDate;
	private String metaDataChunk;

	public RefDataDto getCause() {
		return cause;
	}

	public void setCause(RefDataDto cause) {
		this.cause = cause;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getMetaDataChunk() {
		return metaDataChunk;
	}

	public void setMetaDataChunk(String metaDataChunk) {
		this.metaDataChunk = metaDataChunk;
	}

}
