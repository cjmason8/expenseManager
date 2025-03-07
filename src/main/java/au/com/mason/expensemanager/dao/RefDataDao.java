package au.com.mason.expensemanager.dao;

import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.domain.RefDataType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

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

		return entityManager.createNativeQuery(sql, RefData.class).getResultList();
	}
	
}
