package au.com.mason.expensemanager.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NamedQueries(value = {@NamedQuery(name = MetadataKey.GET_ALL, query = "FROM MetadataKey ORDER BY name"),})
@Entity
@Table(name = "metadatakeys")
@Getter
@Setter
@NoArgsConstructor
public class MetadataKey {

	public static final String GET_ALL = "MetadataKey.Repository.GetAll";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "metadatakeys_seq")
	@SequenceGenerator(name = "metadatakeys_seq", sequenceName = "metadatakeys_seq", allocationSize = 1)
	private long id;

	private String name;

}
