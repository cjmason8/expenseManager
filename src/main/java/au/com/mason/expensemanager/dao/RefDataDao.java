package au.com.mason.expensemanager.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.domain.RefDataType;
import au.com.mason.expensemanager.domain.Statics;

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
		query.setMaxResults(Statics.MAX_RESULTS.getIntValue());

		return query.getResultList();
	}	
	
	public List<RefData> getAllWithEmailKey() {
		Query query = entityManager.createNamedQuery(RefData.GET_ALL_WITH_EMAIL_KEY, RefData.class);

		return query.getResultList();
	}
	
	public List<RefData> findRefDatas(RefData refData) {
		String sql = "SELECT * from refdata where deleted = false ";
		if (refData.getType() != null) {
			sql += " AND type = '" + refData.getType() + "' ";
		}
		if (refData.getDescription() != null) {
			sql += " AND LOWER(description) like '%" + refData.getDescription().toLowerCase() + "%'";
		}
		if (refData.getMetaData() != null) {
			if (refData.getMetaData() != null) {
				for (String val : refData.getMetaData().keySet()) {
					sql += " AND metaData->>'" + val + "' = '" + refData.getMetaData().get(val) + "' ";
				}
			}
		}
		sql += "ORDER BY type,description";

		Query query = entityManager.createNativeQuery(sql, RefData.class);
		query.setMaxResults(Statics.MAX_RESULTS.getIntValue());

		return query.getResultList();
	}
	
}
