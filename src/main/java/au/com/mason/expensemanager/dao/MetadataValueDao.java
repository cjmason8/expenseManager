package au.com.mason.expensemanager.dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import au.com.mason.expensemanager.domain.MetadataValue;

@Repository
@Transactional
public class MetadataValueDao extends BaseDao<MetadataValue> {

	public MetadataValueDao(@Qualifier("entityManagerFactory") EntityManager entityManager) {
		super(MetadataValue.class, entityManager);
	}

	@SuppressWarnings("unchecked")
	public List<MetadataValue> getAll() {
		Query query = entityManager.createNamedQuery(MetadataValue.GET_ALL, MetadataValue.class);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<MetadataValue> getAllByKey(Long metadataKeyId) {
		Query query = entityManager.createNamedQuery(MetadataValue.GET_ALL_BY_KEY, MetadataValue.class);
		query.setParameter("metadataKeyId", metadataKeyId);
		return query.getResultList();
	}

}
