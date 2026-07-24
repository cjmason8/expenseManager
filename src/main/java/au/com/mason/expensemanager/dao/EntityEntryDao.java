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

	public List<EntityEntry> getAll() {
		return entityManager.createNamedQuery(EntityEntry.GET_ALL, EntityEntry.class).getResultList();
	}

	public List<EntityEntry> getAllByType(EntityType type) {
		return entityManager.createNamedQuery(EntityEntry.GET_ALL_BY_TYPE, EntityEntry.class).setParameter("type", type)
			.getResultList();
	}

}
