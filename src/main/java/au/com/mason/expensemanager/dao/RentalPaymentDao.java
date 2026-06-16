package au.com.mason.expensemanager.dao;

import au.com.mason.expensemanager.domain.RentalPayment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class RentalPaymentDao extends BaseDao<RentalPayment> {

	public RentalPaymentDao(@Qualifier("entityManagerFactory") EntityManager entityManager) {
		super(RentalPayment.class, entityManager);
	}

	public List<RentalPayment> getByProperty(String property) {
		Query query = entityManager.createQuery(
				"FROM RentalPayment WHERE property = :property ORDER BY statementFrom DESC",
				RentalPayment.class);
		query.setParameter("property", property);

		return query.getResultList();
	}

}
