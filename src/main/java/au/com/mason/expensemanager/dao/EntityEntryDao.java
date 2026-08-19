package au.com.mason.expensemanager.dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import au.com.mason.expensemanager.domain.EntityEntry;
import au.com.mason.expensemanager.domain.EntityType;

@Repository
@Transactional
public class EntityEntryDao extends BaseDao<EntityEntry> {

	public EntityEntryDao(@Qualifier("entityManagerFactory") EntityManager entityManager) {
		super(EntityEntry.class, entityManager);
	}

	public List<EntityEntry> getAll(boolean includeArchived) {
		String queryName = includeArchived ? EntityEntry.GET_ALL_INCLUDE_ARCHIVED : EntityEntry.GET_ALL;
		return entityManager.createNamedQuery(queryName, EntityEntry.class).getResultList();
	}

	public List<EntityEntry> getAllByType(EntityType type, boolean includeArchived) {
		String queryName = includeArchived ? EntityEntry.GET_ALL_BY_TYPE_INCLUDE_ARCHIVED : EntityEntry.GET_ALL_BY_TYPE;
		return entityManager.createNamedQuery(queryName, EntityEntry.class).setParameter("type", type).getResultList();
	}

	public EntityEntry findByTypeAndName(EntityType type, String name) {
		return entityManager.createNamedQuery(EntityEntry.FIND_BY_TYPE_AND_NAME, EntityEntry.class)
			.setParameter("type", type)
			.setParameter("name", name)
			.getResultStream()
			.findFirst()
			.orElse(null);
	}

}
