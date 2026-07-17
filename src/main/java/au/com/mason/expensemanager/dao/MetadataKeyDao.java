package au.com.mason.expensemanager.dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import au.com.mason.expensemanager.domain.MetadataKey;

@Repository
@Transactional
public class MetadataKeyDao extends BaseDao<MetadataKey> {

	public MetadataKeyDao(@Qualifier("entityManagerFactory") EntityManager entityManager) {
		super(MetadataKey.class, entityManager);
	}

	@SuppressWarnings("unchecked")
	public List<MetadataKey> getAll() {
		Query query = entityManager.createNamedQuery(MetadataKey.GET_ALL, MetadataKey.class);
		return query.getResultList();
	}

	public MetadataKey findByName(String name) {
		List<MetadataKey> results = entityManager.createQuery("FROM MetadataKey WHERE name = :name", MetadataKey.class)
			.setParameter("name", name).setMaxResults(1).getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

}
