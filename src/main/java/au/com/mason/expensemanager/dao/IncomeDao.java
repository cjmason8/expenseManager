package au.com.mason.expensemanager.dao;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import au.com.mason.expensemanager.domain.Income;
import au.com.mason.expensemanager.util.DateUtil;

@Repository
@Transactional
public class IncomeDao implements TransactionDao<Income> {
	
	public Income create(Income income) {
		entityManager.persist(income);

		return income;
	}
	
	public void delete(Income income) {
		if (entityManager.contains(income))
			entityManager.remove(income);
		else
			entityManager.remove(entityManager.merge(income));
		return;
	}
	
	public void deleteById(Long id) {
		Income income = getById(id);
		if (entityManager.contains(income))
			entityManager.remove(income);
		else
			entityManager.remove(entityManager.merge(income));
		return;
	}
	
	@SuppressWarnings("unchecked")
	public List<Income> getAllRecurring(boolean includeAll) {
		
		String sql = "from Income where recurringType IS NOT NULL AND deleted = false";
		if (!includeAll) {
			sql += " AND (endDate is NULL OR endDate >= to_date('" + DateUtil.getFormattedDbDate(LocalDate.now()) + "', 'yyyy-mm-dd'))";
		}
		sql += " ORDER BY entryType.description";
		
		return entityManager.createQuery(sql).getResultList();
	}	
	
	public Income getById(long id) {
		return entityManager.find(Income.class, id);
	}
	
	public Income update(Income income) {
		return entityManager.merge(income);
	}

	public List<Income> getForWeek(LocalDate weekStartDate) {
		String sql = "from Income where recurringType IS NULL "
				+ "AND dueDate >= to_date('" + DateUtil.getFormattedDbDate(weekStartDate) + "', 'yyyy-mm-dd') "
				+ "AND dueDate <= to_date('" + DateUtil.getFormattedDbDate(weekStartDate.plusDays(6)) + "', 'yyyy-mm-dd')"
						+ " ORDER BY dueDate,entryType";

		return entityManager.createQuery(sql).getResultList();
	}

	public List<Income> getPastDate(LocalDate date) {
		String sql = "from Income where recurringType IS NULL"
				+ " AND dueDate > to_date('" + DateUtil.getFormattedDbDate(date) + "', 'yyyy-mm-dd')";

		return entityManager.createQuery(sql).getResultList();
	}

	public List<Income> getPastDate(LocalDate date, Income recurringIncome) {
		String sql = "from Income where dueDate > to_date('" + DateUtil.getFormattedDbDate(date)
				+ "', 'yyyy-mm-dd') and recurringTransaction = :recurringTransaction";
		Query query = entityManager.createQuery(sql);
		query.setParameter("recurringTransaction", recurringIncome);

		return query.getResultList();
	}
	
	public List<Income> getForRecurring(Income recurringIncome) {
		String sql = "from Income where recurringTransaction = :recurringTransaction";
		Query query = entityManager.createQuery(sql);
		query.setParameter("recurringTransaction", recurringIncome);

		return query.getResultList();
	}

	public void deleteTransactions(Long recurringTransactionId) {
		entityManager.createQuery("delete from Income where recurringTransaction.id = " + recurringTransactionId
				+ " AND dueDate > to_date('" + DateUtil.getFormattedDbDate(LocalDate.now()) + "', 'yyyy-mm-dd')")
				.executeUpdate();
	}
	
	// Private fields

	// An EntityManager will be automatically injected from entityManagerFactory
	// setup on DatabaseConfig class.
	@PersistenceContext
	private EntityManager entityManager;

}
