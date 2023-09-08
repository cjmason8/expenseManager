package au.com.mason.expensemanager.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import au.com.mason.expensemanager.dao.MyJsonType;

@NamedQueries(
		value = {
				@NamedQuery(
						name = Donation.GET_ALL,
						query = "FROM Donation ORDER BY dueDate DESC, cause.description"),
		})
@Entity
@Table(name="donations")
@TypeDef(name = "MyJsonType", typeClass = MyJsonType.class)
public class Donation {

	public static final String GET_ALL = "Donation.Repository.GetAll";
	
	public Donation() {}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
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
	@Type(type = "MyJsonType")
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
