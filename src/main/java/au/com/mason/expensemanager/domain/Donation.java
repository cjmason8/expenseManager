package au.com.mason.expensemanager.domain;

import java.time.LocalDate;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import au.com.mason.expensemanager.domain.converter.TimestampLocalDateConverter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NamedQueries(value = {
	@NamedQuery(name = Donation.GET_ALL, query = "FROM Donation ORDER BY dueDate DESC, cause.description"),})
@Entity
@Table(name = "donations")
@Getter
@Setter
@NoArgsConstructor
public class Donation {

	public static final String GET_ALL = "Donation.Repository.GetAll";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "donations_seq")
	@SequenceGenerator(name = "donations_seq", sequenceName = "donations_seq", allocationSize = 1)
	private long id;

	@OneToOne
	@JoinColumn(name = "causeId")
	private RefData cause;

	private String description;

	@Convert(converter = TimestampLocalDateConverter.class)
	private LocalDate dueDate;

	private String notes;

	@OneToOne
	@JoinColumn(name = "documentId")
	private Document document;

	@Column
	@JdbcTypeCode(SqlTypes.JSON)
	private Map<String, String> metaData;

}
