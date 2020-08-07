package au.com.mason.expensemanager.dao;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import au.com.mason.expensemanager.domain.RentalPayment;

@Repository
@Transactional
public class RentalPaymentDao {
	
	public RentalPayment create(RentalPayment rentalPayment) {
		entityManager.persist(rentalPayment);

		return rentalPayment;
	}
	
	public void delete(RentalPayment rentalPayment) {
		if (entityManager.contains(rentalPayment))
			entityManager.remove(rentalPayment);
		else
			entityManager.remove(entityManager.merge(rentalPayment));
		return;
	}
	
	public void deleteById(Long id) {
		RentalPayment rentalPayment = entityManager.find(RentalPayment.class, id);
		entityManager.remove(rentalPayment);
		return;
	}
	
	public RentalPayment getById(long id) {
		return entityManager.find(RentalPayment.class, id);
	}
	
	public RentalPayment update(RentalPayment rentalPayment) {
		return entityManager.merge(rentalPayment);
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

	// Private fields

	// An EntityManager will be automatically injected from entityManagerFactory
	// setup on DatabaseConfig class.
	@PersistenceContext
	private EntityManager entityManager;

}
