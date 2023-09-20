package au.com.mason.expensemanager.domain;

import au.com.mason.expensemanager.dao.MyJsonType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name="transactions")
@DiscriminatorColumn(name = "transactionType")
public abstract class Transaction implements Metadata {
	
	public Transaction() {}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "transactions_seq")
	@SequenceGenerator(name = "transactions_seq", sequenceName = "transactions_seq", allocationSize = 1)
	private long id;

	private BigDecimal amount;
	private Date dueDate;
	private String documentationFilePath;
	
	@OneToOne
	@JoinColumn(name = "documentId")
	private Document document;
	
	@OneToOne
	@JoinColumn(name = "recurringTypeId")
	private RefData recurringType;
	private Date startDate;
	private Date endDate;
	private String notes;
	
    @Column
	@JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metaData;
	
	@OneToOne
	@JoinColumn(name = "entryTypeId")
	private RefData entryType;
	
	private Boolean deleted = Boolean.FALSE;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public LocalDate getDueDate() {
		if (dueDate != null) {
			return new java.sql.Date(dueDate.getTime()).toLocalDate();
		}
	
		return null;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = java.sql.Date.valueOf(dueDate);
	}

	public RefData getRecurringType() {
		return recurringType;
	}

	public void setRecurringType(RefData recurringType) {
		this.recurringType = recurringType;
	}

	public LocalDate getStartDate() {
		if (startDate != null) {
			return new java.sql.Date(startDate.getTime()).toLocalDate();
		}
		
		return null;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = java.sql.Date.valueOf(startDate);
	}

	public LocalDate getEndDate() {
		if (endDate != null) {
			return new java.sql.Date(endDate.getTime()).toLocalDate();
		}
		
		return null;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = java.sql.Date.valueOf(endDate);
	}
	
	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	
	public RefData getEntryType() {
		return entryType;
	}

	public void setEntryType(RefData entryType) {
		this.entryType = entryType;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public Map<String, Object> getMetaData() {
		return metaData;
	}

	public void setMetaData(Map<String, Object> metaData) {
		this.metaData = metaData;
	}
	
	public String getDocumentationFilePath() {
		return documentationFilePath;
	}

	public void setDocumentationFilePath(String documentationFilePath) {
		this.documentationFilePath = documentationFilePath;
	}
	
	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public abstract void setRecurringTransaction(Transaction recurringTransaction);
	
	public abstract Transaction getRecurringTransaction();

}
