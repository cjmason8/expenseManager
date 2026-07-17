package au.com.mason.expensemanager.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NamedQueries(value = {
	@NamedQuery(name = MetadataValue.GET_ALL, query = "FROM MetadataValue ORDER BY metadataKey.name, value"),
	@NamedQuery(name = MetadataValue.GET_ALL_BY_KEY, query = "FROM MetadataValue WHERE metadataKey.id = :metadataKeyId ORDER BY value"),})
@Entity
@Table(name = "metadatavalues")
@Getter
@Setter
@NoArgsConstructor
public class MetadataValue {

	public static final String GET_ALL = "MetadataValue.Repository.GetAll";
	public static final String GET_ALL_BY_KEY = "MetadataValue.Repository.GetAllByKey";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "metadatavalues_seq")
	@SequenceGenerator(name = "metadatavalues_seq", sequenceName = "metadatavalues_seq", allocationSize = 1)
	private long id;

	private String value;

	@ManyToOne
	@JoinColumn(name = "metadatakeyid")
	private MetadataKey metadataKey;

}
