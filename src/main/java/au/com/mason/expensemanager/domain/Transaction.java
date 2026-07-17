package au.com.mason.expensemanager.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import au.com.mason.expensemanager.domain.converter.TimestampLocalDateConverter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transactions")
@DiscriminatorColumn(name = "transactionType")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "transactions_seq")
	@SequenceGenerator(name = "transactions_seq", sequenceName = "transactions_seq", allocationSize = 1)
	private long id;

	private BigDecimal amount;
	private String documentationFilePath;

	@OneToOne
	@JoinColumn(name = "documentId")
	private Document document;

	@OneToOne
	@JoinColumn(name = "recurringTypeId")
	private RefData recurringType;

	@Convert(converter = TimestampLocalDateConverter.class)
	private LocalDate dueDate;

	@Convert(converter = TimestampLocalDateConverter.class)
	private LocalDate startDate;

	@Convert(converter = TimestampLocalDateConverter.class)
	private LocalDate endDate;

	private String notes;

	@Transient
	private List<EntityMetadata> entityMetadata = new ArrayList<>();

	@Transient
	private Map<String, Object> metaData;

	@OneToOne
	@JoinColumn(name = "entryTypeId")
	private RefData entryType;

	private Boolean deleted = Boolean.FALSE;

	public abstract void setRecurringTransaction(Transaction recurringTransaction);

	public abstract Transaction getRecurringTransaction();

}
