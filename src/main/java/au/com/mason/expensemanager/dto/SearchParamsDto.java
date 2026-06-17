package au.com.mason.expensemanager.dto;

public class SearchParamsDto {
	private RefDataDto transactionType;
	private String keyWords;
	private String metaDataChunk;
	private String startDateString;
	private String endDateString;

	public RefDataDto getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(RefDataDto transactionType) {
		this.transactionType = transactionType;
	}

	public String getKeyWords() {
		return keyWords;
	}

	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}

	public String getMetaDataChunk() {
		return metaDataChunk;
	}

	public void setMetaDataChunk(String metaDataChunk) {
		this.metaDataChunk = metaDataChunk;
	}

	public String getStartDateString() {
		return startDateString;
	}

	public void setStartDateString(String startDateString) {
		this.startDateString = startDateString;
	}

	public String getEndDateString() {
		return endDateString;
	}

	public void setEndDateString(String endDateString) {
		this.endDateString = endDateString;
	}

}
