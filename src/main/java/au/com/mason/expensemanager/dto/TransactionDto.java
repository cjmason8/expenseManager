package au.com.mason.expensemanager.dto;

public class TransactionDto {

	private Long id;
	private RefDataDto transactionType;
	private String amount;
	private String dueDateString;

	private RefDataDto recurringType;
	private String startDateString;
	private String endDateString;
	private String week;
	private String notes;
	private String metaDataChunk;
	private DocumentDto documentDto;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RefDataDto getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(RefDataDto transactionType) {
		this.transactionType = transactionType;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public RefDataDto getRecurringType() {
		return recurringType;
	}

	public void setRecurringType(RefDataDto recurringType) {
		this.recurringType = recurringType;
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

	public String getWeek() {
		return week;
	}

	public void setWeek(String week) {
		this.week = week;
	}

	public String getDueDateString() {
		return dueDateString;
	}

	public void setDueDateString(String dueDateString) {
		this.dueDateString = dueDateString;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getMetaDataChunk() {
		return metaDataChunk;
	}

	public void setMetaDataChunk(String metaDataChunk) {
		this.metaDataChunk = metaDataChunk;
	}

	public DocumentDto getDocumentDto() {
		return documentDto;
	}

	public void setDocumentDto(DocumentDto documentDto) {
		this.documentDto = documentDto;
	}

}
