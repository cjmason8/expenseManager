package au.com.mason.expensemanager.dao;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.domain.RefDataType;

@Repository
@Transactional
public class RefDataDao extends BaseDao<RefData> {

	public RefDataDao(@Qualifier("entityManagerFactory") EntityManager entityManager) {
		super(RefData.class, entityManager);
	}

	@SuppressWarnings("unchecked")
	public List<RefData> getAllByType(String type) {
		Query query = entityManager.createNamedQuery(RefData.GET_ALL_BY_TYPE, RefData.class);
		query.setParameter("type", RefDataType.valueOf(type));
		return query.getResultList();
	}

	public List<RefData> getAll() {
		Query query = entityManager.createNamedQuery(RefData.GET_ALL, RefData.class);

		return query.getResultList();
	}

	public List<RefData> getAllWithEmailKey() {
		return entityManager.createNamedQuery(RefData.GET_ALL_WITH_EMAIL_KEY, RefData.class).getResultList();
	}

	public List<RefData> findRefDatas(RefData refData) {
		StringBuilder jpql = new StringBuilder("FROM RefData r WHERE r.deleted = false ");
		if (refData.getType() != null) {
			jpql.append("AND r.type = :type ");
		}
		if (refData.getDescription() != null) {
			jpql.append("AND lower(r.description) LIKE lower(:description) ");
		}
		jpql.append("ORDER BY r.type, r.description");

		TypedQuery<RefData> query = entityManager.createQuery(jpql.toString(), RefData.class);
		if (refData.getType() != null) {
			query.setParameter("type", refData.getType());
		}
		if (refData.getDescription() != null) {
			query.setParameter("description", "%" + refData.getDescription().toLowerCase() + "%");
		}
		List<RefData> list = query.getResultList();
		if (refData.getMetaData() != null && !refData.getMetaData().isEmpty()) {
			list = list.stream().filter(r -> refDataMatchesMetaData(r, refData.getMetaData()))
				.collect(Collectors.toList());
		}
		return list;
	}

	private static boolean refDataMatchesMetaData(RefData r, Map<String, String> criteria) {
		if (r.getMetaData() == null) {
			return false;
		}
		for (Map.Entry<String, String> e : criteria.entrySet()) {
			if (!Objects.equals(r.getMetaData().get(e.getKey()), e.getValue())) {
				return false;
			}
		}
		return true;
	}

}
