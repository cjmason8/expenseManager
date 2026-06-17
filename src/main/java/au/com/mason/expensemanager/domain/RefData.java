package au.com.mason.expensemanager.domain;

import java.util.Map;

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

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import au.com.mason.expensemanager.processor.EmailProcessor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NamedQueries(value = {
	@NamedQuery(name = RefData.GET_ALL_BY_TYPE, query = "FROM RefData WHERE type = :type ORDER BY type, description"),
	@NamedQuery(name = RefData.GET_ALL, query = "FROM RefData WHERE deleted = false ORDER BY type, description"),
	@NamedQuery(name = RefData.GET_ALL_WITH_EMAIL_KEY, query = "FROM RefData WHERE emailKey IS NOT NULL"),})
@Entity
@Table(name = "refdata")
@Getter
@Setter
@NoArgsConstructor
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

	public String getDescriptionUpper() {
		return description.toUpperCase().replace(" ", "_");
	}

}
