package au.com.mason.expensemanager.repository;

import au.com.mason.expensemanager.repository.BaseRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.domain.RefDataType;
import au.com.mason.expensemanager.domain.Statics;

@Repository
@Transactional
public abstract class RefDataRepository extends BaseRepository<RefData> {
	
	protected RefDataRepository(EntityManager em) {
		super(em, RefData.class);
	}

	public List<RefData> getAll(String type) {
		var results = new ArrayList<RefData>();
		findAll().forEach(results::add);
		results.stream().filter(refData -> refData.getType().equals(RefDataType.valueOf(type))).collect(Collectors.toList())
				.sort(Comparator.comparing(RefData::getType).thenComparing(RefData::getDescription));

		return results.stream().limit(Statics.MAX_RESULTS.getIntValue()).collect(Collectors.toList());
	}
	
	public List<RefData> getAll() {
		var results = new ArrayList<RefData>();
		findAll().forEach(results::add);
		results.sort(Comparator.comparing(RefData::getType).thenComparing(RefData::getDescription));

		return results.stream().limit(Statics.MAX_RESULTS.getIntValue()).collect(Collectors.toList());
	}
	
	public List<RefData> getAllWithEmailKey() {
		Query query = em.createNamedQuery(RefData.FIND_ALL_BY_EMAIL_KEY, RefData.class);

		return query.getResultList();
	}
	
	public List<RefData> findRefDatas(RefData refData) {
		String sql = "SELECT * from refdata where ";
		boolean addAnd = false;
		if (refData.getType() != null) {
			addAnd = true;
			sql += " type = '" + refData.getType() + "' ";
		}
		if (refData.getDescription() != null) {
			if (addAnd) {
				sql += "AND";
			}
			addAnd = true;
			sql += " LOWER(description) like '%" + refData.getDescription().toLowerCase() + "%'";
		}
		if (refData.getMetaData() != null) {
			if (refData.getMetaData() != null) {
				for (String val : refData.getMetaData().keySet()) {
					if (addAnd) {
						sql += "AND";
					}
					addAnd = true;
					sql += " metaData->>'" + val + "' = '" + refData.getMetaData().get(val) + "' ";
				}
			}
		}
		sql += "ORDER BY type,description";

		Query query = em.createNativeQuery(sql, RefData.class);
		query.setMaxResults(Statics.MAX_RESULTS.getIntValue());

		return query.getResultList();
	}

}
