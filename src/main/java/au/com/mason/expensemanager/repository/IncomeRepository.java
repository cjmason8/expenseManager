package au.com.mason.expensemanager.repository;

import au.com.mason.expensemanager.repository.TransactionRepository;
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
public abstract class IncomeRepository extends BaseRepository<Income> implements TransactionRepository<Income> {

	protected IncomeRepository(EntityManager em) {
		super(em, Income.class);
	}

	@SuppressWarnings("unchecked")
	public List<Income> getAllRecurring(boolean includeAll) {
		
		String sql = "from Income where recurringType IS NOT NULL AND deleted = false";
		if (!includeAll) {
			sql += " AND (endDate is NULL OR endDate >= to_date('" + DateUtil.getFormattedDbDate(LocalDate.now()) + "', 'yyyy-mm-dd'))";
		}
		sql += " ORDER BY entryType.description";
		
		return em.createQuery(sql).getResultList();
	}	
	
	public List<Income> getForWeek(LocalDate weekStartDate) {
		String sql = "from Income where recurringType IS NULL "
				+ "AND dueDate >= to_date('" + DateUtil.getFormattedDbDate(weekStartDate) + "', 'yyyy-mm-dd') "
				+ "AND dueDate <= to_date('" + DateUtil.getFormattedDbDate(weekStartDate.plusDays(6)) + "', 'yyyy-mm-dd')"
						+ " ORDER BY dueDate,entryType";

		return em.createQuery(sql).getResultList();
	}

	public List<Income> getPastDate(LocalDate date) {
		String sql = "from Income where recurringType IS NULL"
				+ " AND dueDate > to_date('" + DateUtil.getFormattedDbDate(date) + "', 'yyyy-mm-dd')";

		return em.createQuery(sql).getResultList();
	}

	public List<Income> getPastDate(LocalDate date, Income recurringIncome) {
		String sql = "from Income where dueDate > to_date('" + DateUtil.getFormattedDbDate(date)
				+ "', 'yyyy-mm-dd') and recurringTransaction = :recurringTransaction";
		Query query = em.createQuery(sql);
		query.setParameter("recurringTransaction", recurringIncome);

		return query.getResultList();
	}
	
	public List<Income> getForRecurring(Income recurringIncome) {
		String sql = "from Income where recurringTransaction = :recurringTransaction";
		Query query = em.createQuery(sql);
		query.setParameter("recurringTransaction", recurringIncome);

		return query.getResultList();
	}

	public void deleteTransactions(Long recurringTransactionId) {
		em.createQuery("delete from Income where recurringTransaction.id = " + recurringTransactionId
				+ " AND dueDate > to_date('" + DateUtil.getFormattedDbDate(LocalDate.now()) + "', 'yyyy-mm-dd')")
				.executeUpdate();
	}
}
