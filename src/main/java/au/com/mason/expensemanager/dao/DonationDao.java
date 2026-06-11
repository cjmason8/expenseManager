package au.com.mason.expensemanager.dao;

import au.com.mason.expensemanager.domain.Donation;
import au.com.mason.expensemanager.dto.DonationSearchDto;
import au.com.mason.expensemanager.util.DateUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
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
		return entityManager.createNamedQuery(Donation.GET_ALL, Donation.class).getResultList();
	}

	public List<Donation> findDonations(DonationSearchDto donationSearchDto) {
		String sql = "SELECT d.* from donations d LEFT JOIN refdata r on d.causeId = r.id where ";
		Map<String, Object> parameters = new HashMap<>();
		boolean addAnd = false;
		if (donationSearchDto.getCause() != null) {
			addAnd = true;
			sql += "r.description = :causeDescription";
			parameters.put("causeDescription", donationSearchDto.getCause().getDescription());
		}
		if (!StringUtils.isBlank(donationSearchDto.getStartDate())) {
			if (addAnd) {
				sql += " AND ";
			}
			addAnd = true;
			sql += "d.duedate >= :startDate ";
			parameters.put("startDate", DateUtil.getFormattedDate(donationSearchDto.getStartDate()));
		}
		if (!StringUtils.isBlank(donationSearchDto.getEndDate())) {
			if (addAnd) {
				sql += " AND ";
			}
			addAnd = true;
			sql += "d.duedate <= :endDate ";
			parameters.put("endDate", DateUtil.getFormattedDate(donationSearchDto.getEndDate()));
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
		sql += " ORDER BY duedate DESC,r.description";

		Query query = entityManager.createNativeQuery(sql, Donation.class);
		parameters.forEach(query::setParameter);
		return query.getResultList();
	}

}
