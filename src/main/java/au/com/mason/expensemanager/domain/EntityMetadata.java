package au.com.mason.expensemanager.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
	@NamedQuery(name = EntityMetadata.GET_BY_ENTITY, query = "SELECT em FROM EntityMetadata em JOIN FETCH em.metadataValue mv JOIN FETCH mv.metadataKey WHERE em.type = :type AND em.entityId = :entityId"),
	@NamedQuery(name = EntityMetadata.GET_BY_ENTITIES, query = "SELECT em FROM EntityMetadata em JOIN FETCH em.metadataValue mv JOIN FETCH mv.metadataKey WHERE em.type = :type AND em.entityId IN :entityIds"),})
@Entity
@Table(name = "entitymetadata")
@Getter
@Setter
@NoArgsConstructor
public class EntityMetadata {

	public static final String GET_BY_ENTITY = "EntityMetadata.Repository.GetByEntity";
	public static final String GET_BY_ENTITIES = "EntityMetadata.Repository.GetByEntities";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "entitymetadata_seq")
	@SequenceGenerator(name = "entitymetadata_seq", sequenceName = "entitymetadata_seq", allocationSize = 1)
	private long id;

	@Enumerated(EnumType.STRING)
	private EntityMetadataType type;

	private String entityId;

	@ManyToOne
	@JoinColumn(name = "metadatavalueid")
	private MetadataValue metadataValue;

}
