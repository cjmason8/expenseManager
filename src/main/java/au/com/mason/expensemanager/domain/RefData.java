package au.com.mason.expensemanager.domain;

import au.com.mason.expensemanager.dao.MyJsonType;
import au.com.mason.expensemanager.processor.EmailProcessor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
public class RefData {

	public static final String GET_ALL_BY_TYPE = "RefData.Repository.GetAllByType";
	public static final String GET_ALL = "RefData.Repository.GetAll";
	public static final String GET_ALL_WITH_EMAIL_KEY = "RefData.Repository.GetAllWithEmailKey";
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "refdata_seq")
	@SequenceGenerator(name = "refdata_seq", sequenceName = "refdata_seq", allocationSize = 1)
	private long id;

	private String description;
	@Enumerated(EnumType.STRING)
	private RefDataType type;
	
    @Column
	@JdbcTypeCode(SqlTypes.JSON)
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
