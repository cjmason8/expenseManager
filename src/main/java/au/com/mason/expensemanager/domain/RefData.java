package au.com.mason.expensemanager.domain;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import au.com.mason.expensemanager.dao.MyJsonType;
import au.com.mason.expensemanager.processor.EmailProcessor;

@NamedQueries(
		value = {
				@NamedQuery(
						name = RefData.GET_ALL_BY_TYPE,
						query = "FROM RefData WHERE type = :type ORDER BY type, description"),
				@NamedQuery(
						name = RefData.GET_ALL,
						query = "FROM RefData ORDER BY type, description"),
				@NamedQuery(
						name = RefData.GET_ALL_WITH_EMAIL_KEY,
						query = "FROM RefData WHERE emailKey IS NOT NULL"),
		})
@Entity
@Table(name="refdata")
@TypeDef(name = "MyJsonType", typeClass = MyJsonType.class)
public class RefData {

	public static final String GET_ALL_BY_TYPE = "RefData.Repository.GetAllByType";
	public static final String GET_ALL = "RefData.Repository.GetAll";
	public static final String GET_ALL_WITH_EMAIL_KEY = "RefData.Repository.GetAllWithEmailKey";
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String description;
	@Enumerated(EnumType.STRING)
	private RefDataType type;
	
    @Column
	@Type(type = "MyJsonType")
    private Map<String, String> metaData;
    
	private String emailKey;
	
	@Enumerated(EnumType.STRING)
	private EmailProcessor emailProcessor;

	private boolean deleted;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public RefDataType getType() {
		return type;
	}
	
	public void setType(RefDataType type) {
		this.type = type;
	}
	
	public String getDescriptionUpper() {
		return description.toUpperCase().replace(" ", "_");
	}

	public Map<String, String> getMetaData() {
		return metaData;
	}

	public void setMetaData(Map<String, String> metaData) {
		this.metaData = metaData;
	}

	public String getEmailKey() {
		return emailKey;
	}

	public void setEmailKey(String emailKey) {
		this.emailKey = emailKey;
	}

	public EmailProcessor getEmailProcessor() {
		return emailProcessor;
	}

	public void setEmailProcessor(EmailProcessor emailProcessor) {
		this.emailProcessor = emailProcessor;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isDeleted() {
		return this.deleted;
	}
	
}
