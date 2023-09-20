package au.com.mason.expensemanager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@NamedQueries(
		value = {
				@NamedQuery(
						name = Donation.GET_ALL,
						query = "FROM Donation ORDER BY dueDate DESC, cause.description"),
		})
@Entity
@Table(name="donations")
public class Donation {

	public static final String GET_ALL = "Donation.Repository.GetAll";
	
	public Donation() {}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "donations_seq")
	@SequenceGenerator(name = "donations_seq", sequenceName = "donations_seq", allocationSize = 1)
	private long id;

	@OneToOne
	@JoinColumn(name = "causeId")
	private RefData cause;
	
	private String description;
	private Date dueDate;
	private String notes;
	@OneToOne
	@JoinColumn(name = "documentId")
	private Document document; 
	
    @Column
	@JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> metaData;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public RefData getCause() {
		return cause;
	}

	public void setCause(RefData cause) {
		this.cause = cause;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public Map<String, String> getMetaData() {
		return metaData;
	}

	public void setMetaData(Map<String, String> metaData) {
		this.metaData = metaData;
	}

}
