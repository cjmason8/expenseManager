package au.com.mason.expensemanager.dao;

import au.com.mason.expensemanager.domain.Donation;
import au.com.mason.expensemanager.domain.Statics;
import au.com.mason.expensemanager.dto.DonationSearchDto;
import au.com.mason.expensemanager.util.DateUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class DonationDao extends BaseDao<Donation> {
	
	private Gson gson = new GsonBuilder().serializeNulls().create();

	public DonationDao(@Qualifier("entityManagerFactory") EntityManager entityManager) {
		super(Donation.class, entityManager);
	}

	public List<Donation> getAll() {
		Query query = entityManager.createNamedQuery(Donation.GET_ALL, Donation.class);
		query.setMaxResults(Statics.MAX_RESULTS.getIntValue());
		return query.getResultList();
	}
	
	public List<Donation> findDonations(DonationSearchDto donationSearchDto) {
		String sql = "SELECT * from donations d LEFT JOIN refdata r on d.causeId = r.id where ";
		boolean addAnd = false;
		if (donationSearchDto.getCause() != null) {
			addAnd = true;
			sql += "r.description = '" + donationSearchDto.getCause().getDescription() + "'";
		}
		if (donationSearchDto.getStartDate() != null) {
			if (addAnd) {
				sql += " AND ";
			}
			addAnd = true;
			sql += "d.dueDate >= to_date('" + DateUtil.getFormattedDbDate(donationSearchDto.getStartDate()) + "', 'yyyy-mm-dd') ";
		}
		if (donationSearchDto.getEndDate() != null) {
			if (addAnd) {
				sql += " AND ";
			}
			addAnd = true;
			sql += "d.dueDate <= to_date('" + DateUtil.getFormattedDbDate(donationSearchDto.getEndDate()) + "', 'yyyy-mm-dd') ";
		}
		if (donationSearchDto.getMetaDataChunk() != null) {
			Map<String, String> metaData = gson.fromJson(donationSearchDto.getMetaDataChunk(), Map.class);
			if (addAnd) {
				sql += " AND ";
			}
			addAnd = true;
			boolean firstOne = true;
			for (String val : metaData.keySet()) {
				if (!firstOne) {
					sql += " AND ";
				}
				firstOne = false;
				sql += "d.metaData->>'" + val + "' = '" + metaData.get(val) + "'";
			}
		}
		sql += " ORDER BY dueDate DESC,r.description";
		
		Query query = entityManager.createNativeQuery(sql, Donation.class);
		query.setMaxResults(Statics.MAX_RESULTS.getIntValue());
		return query.getResultList();
	}

}
