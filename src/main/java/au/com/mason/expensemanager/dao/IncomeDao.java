package au.com.mason.expensemanager.dao;

import au.com.mason.expensemanager.domain.Income;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class IncomeDao extends BaseDao<Income> implements TransactionDao<Income> {

	public IncomeDao(@Qualifier("entityManagerFactory") EntityManager entityManager) {
		super(Income.class, entityManager);
	}

	@Override
	public Income getById(long id) {
		return super.getById(Long.valueOf(id));
	}

	public List<Income> getAllRecurring(boolean includeAll) {
		String sql = "from Income where recurringType IS NOT NULL AND deleted = false";
		if (!includeAll) {
			sql += " AND (endDate is NULL OR endDate >= :today)";
		}
		sql += " ORDER BY entryType.description";

		Query query = entityManager.createQuery(sql);
		if (!includeAll) {
			query.setParameter("today", LocalDate.now());
		}

		return query.getResultList();
	}

	public List<Income> getForWeek(LocalDate weekStartDate) {
		Query query = entityManager.createQuery(
				"from Income where recurringType IS NULL AND dueDate >= :weekStartDate "
						+ "AND dueDate <= :weekEndDate ORDER BY dueDate,entryType.type");
		query.setParameter("weekStartDate", weekStartDate);
		query.setParameter("weekEndDate", weekStartDate.plusDays(6));

		return query.getResultList();
	}

	public List<Income> getPastDate(LocalDate date) {
		Query query = entityManager.createQuery(
				"from Income where recurringType IS NULL AND dueDate > :date");
		query.setParameter("date", date);

		return query.getResultList();
	}

	public List<Income> getPastDate(LocalDate date, Income recurringIncome) {
		Query query = entityManager.createQuery(
				"from Income where dueDate > :date and recurringTransaction = :recurringTransaction");
		query.setParameter("date", date);
		query.setParameter("recurringTransaction", recurringIncome);

		return query.getResultList();
	}

	public List<Income> getForRecurring(Income recurringIncome) {
		Query query = entityManager.createQuery("from Income where recurringTransaction = :recurringTransaction");
		query.setParameter("recurringTransaction", recurringIncome);

		return query.getResultList();
	}

	public void deleteTransactions(Long recurringTransactionId) {
		entityManager.createQuery(
				"delete from Income where recurringTransaction.id = :recurringTransactionId AND dueDate > :today")
				.setParameter("recurringTransactionId", recurringTransactionId)
				.setParameter("today", LocalDate.now())
				.executeUpdate();
	}

}
