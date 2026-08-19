package au.com.mason.expensemanager.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NamedQueries(value = {
	@NamedQuery(name = EntityEntry.GET_ALL, query = "FROM EntityEntry WHERE isArchived = false ORDER BY name"),
	@NamedQuery(name = EntityEntry.GET_ALL_INCLUDE_ARCHIVED, query = "FROM EntityEntry ORDER BY name"),
	@NamedQuery(name = EntityEntry.GET_ALL_BY_TYPE, query = "FROM EntityEntry WHERE type = :type AND isArchived = false ORDER BY name"),
	@NamedQuery(name = EntityEntry.GET_ALL_BY_TYPE_INCLUDE_ARCHIVED, query = "FROM EntityEntry WHERE type = :type ORDER BY name"),
	@NamedQuery(name = EntityEntry.FIND_BY_TYPE_AND_NAME, query = "FROM EntityEntry WHERE type = :type AND name = :name AND isArchived = false"),})
@Entity
@Table(name = "entities")
@Getter
@Setter
@NoArgsConstructor
public class EntityEntry {

	public static final String GET_ALL = "EntityEntry.Repository.GetAll";
	public static final String GET_ALL_INCLUDE_ARCHIVED = "EntityEntry.Repository.GetAllIncludeArchived";
	public static final String GET_ALL_BY_TYPE = "EntityEntry.Repository.GetAllByType";
	public static final String GET_ALL_BY_TYPE_INCLUDE_ARCHIVED = "EntityEntry.Repository.GetAllByTypeIncludeArchived";
	public static final String FIND_BY_TYPE_AND_NAME = "EntityEntry.Repository.FindByTypeAndName";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "entities_seq")
	@SequenceGenerator(name = "entities_seq", sequenceName = "entities_seq", allocationSize = 1)
	private long id;

	private String name;

	private String description;

	@Enumerated(EnumType.STRING)
	private EntityType type;

	@OneToOne
	@JoinColumn(name = "documentId")
	private Document document;

	@JdbcTypeCode(SqlTypes.JSON)
	private Map<String, Object> data;

	private boolean isArchived;

	@Transient
	private List<EntityMetadata> entityMetadata = new ArrayList<>();

	@Transient
	private Map<String, String> metaData;

}
