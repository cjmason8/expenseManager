package au.com.mason.expensemanager.dao;

import au.com.mason.expensemanager.domain.RentalPayment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class RentalPaymentDao extends BaseDao<RentalPayment> {

	public RentalPaymentDao(@Qualifier("entityManagerFactory") EntityManager entityManager) {
		super(RentalPayment.class, entityManager);
	}

	public List<RentalPayment> getAll(String property, LocalDate startDate, LocalDate endDate) {
		int endDateYear = endDate.getYear();
		int startDateYear = startDate.getYear();
		String sql = "FROM RentalPayment WHERE property = :property AND ((statementFrom >= :startDate AND statementTo <= :endDate) "
				+ "OR (statementTo >= :startDate AND statementTo <= :endDate AND MONTH(statementFrom) = 6 AND YEAR(statementFrom) = :startDateYear) "
				+ "OR (statementFrom <= :endDate AND statementFrom >= :startDate AND MONTH(statementTo) = 7 AND YEAR(statementTo) = :endDateYear)) ";
		sql += "ORDER BY statementFrom DESC";
		Query query = entityManager.createQuery(sql);
		query.setParameter("property", property);
		query.setParameter("startDate", startDate);
		query.setParameter("endDate", endDate);
		query.setParameter("startDateYear", startDateYear);
		query.setParameter("endDateYear", endDateYear);

		return query.getResultList();
	}

}
