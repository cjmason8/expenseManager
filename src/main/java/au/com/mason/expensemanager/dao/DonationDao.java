package au.com.mason.expensemanager.dao;

import au.com.mason.expensemanager.domain.Donation;
import au.com.mason.expensemanager.dto.DonationSearchDto;
import au.com.mason.expensemanager.dto.RefDataDto;
import au.com.mason.expensemanager.util.DateUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
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
		StringBuilder jpql = new StringBuilder("SELECT d FROM Donation d WHERE 1=1 ");
		if (donationSearchDto.getCause() != null) {
			RefDataDto c = donationSearchDto.getCause();
			if (c.getId() != null) {
				jpql.append("AND d.cause.id = :causeId ");
			} else if (c.getDescription() != null) {
				jpql.append("AND d.cause.description = :causeDescription ");
			}
		}
		if (donationSearchDto.getStartDate() != null) {
			jpql.append("AND d.dueDate >= to_date(:startDate, 'yyyy-mm-dd') ");
		}
		if (donationSearchDto.getEndDate() != null) {
			jpql.append("AND d.dueDate <= to_date(:endDate, 'yyyy-mm-dd') ");
		}
		jpql.append("ORDER BY d.dueDate DESC, d.cause.description");

		TypedQuery<Donation> query = entityManager.createQuery(jpql.toString(), Donation.class);
		if (donationSearchDto.getCause() != null) {
			RefDataDto c = donationSearchDto.getCause();
			if (c.getId() != null) {
				query.setParameter("causeId", c.getId());
			} else if (c.getDescription() != null) {
				query.setParameter("causeDescription", c.getDescription());
			}
		}
		if (donationSearchDto.getStartDate() != null) {
			query.setParameter("startDate", DateUtil.getFormattedDbDate(donationSearchDto.getStartDate()));
		}
		if (donationSearchDto.getEndDate() != null) {
			query.setParameter("endDate", DateUtil.getFormattedDbDate(donationSearchDto.getEndDate()));
		}

		List<Donation> results = query.getResultList();
		if (donationSearchDto.getMetaDataChunk() != null) {
			Map<String, String> metaData = gson.fromJson(donationSearchDto.getMetaDataChunk(), Map.class);
			results = results.stream()
					.filter(d -> donationMatchesMetaData(d, metaData))
					.collect(Collectors.toList());
		}
		return results;
	}

	private static boolean donationMatchesMetaData(Donation d, Map<String, String> metaData) {
		if (d.getMetaData() == null) {
			return false;
		}
		for (Map.Entry<String, String> e : metaData.entrySet()) {
			if (!Objects.equals(d.getMetaData().get(e.getKey()), e.getValue())) {
				return false;
			}
		}
		return true;
	}

}
