package au.com.mason.expensemanager.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import au.com.mason.expensemanager.domain.EntityMetadata;
import au.com.mason.expensemanager.domain.EntityMetadataType;

@Repository
@Transactional
public class EntityMetadataDao extends BaseDao<EntityMetadata> {

	public EntityMetadataDao(@Qualifier("entityManagerFactory") EntityManager entityManager) {
		super(EntityMetadata.class, entityManager);
	}

	@SuppressWarnings("unchecked")
	public List<EntityMetadata> getByEntity(EntityMetadataType type, String entityId) {
		Query query = entityManager.createNamedQuery(EntityMetadata.GET_BY_ENTITY, EntityMetadata.class);
		query.setParameter("type", type);
		query.setParameter("entityId", entityId);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<EntityMetadata> getByEntities(EntityMetadataType type, Collection<String> entityIds) {
		if (entityIds == null || entityIds.isEmpty()) {
			return Collections.emptyList();
		}
		Query query = entityManager.createNamedQuery(EntityMetadata.GET_BY_ENTITIES, EntityMetadata.class);
		query.setParameter("type", type);
		query.setParameter("entityIds", entityIds);
		return query.getResultList();
	}

	public void deleteByEntity(EntityMetadataType type, String entityId) {
		entityManager.createQuery("DELETE FROM EntityMetadata em WHERE em.type = :type AND em.entityId = :entityId")
			.setParameter("type", type).setParameter("entityId", entityId).executeUpdate();
	}

}
