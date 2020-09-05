package au.com.mason.expensemanager.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.domain.Statics;
import au.com.mason.expensemanager.dto.SearchParamsDto;
import au.com.mason.expensemanager.util.DateUtil;

@Repository
@Transactional
public class ExpenseDao extends BaseDao<Expense> implements TransactionDao<Expense> {
	
	@Autowired
	private NotificationDao notificationDao;
	
	public Expense create(Expense expense) {
		entityManager.persist(expense);

		return expense;
	}
	
	public void delete(Expense expense) {
		notificationDao.deleteForExpense(expense);

		if (entityManager.contains(expense))
			entityManager.remove(expense);
		else
			entityManager.remove(entityManager.merge(expense));
		return;
	}
	
	@SuppressWarnings("unchecked")
	public List<Expense> getAllRecurring(boolean includeAll) {
		String sql = "from Expense where recurringType IS NOT NULL AND deleted = false";
		if (!includeAll) {
			sql += " AND (endDate is NULL OR endDate >= to_date('" + DateUtil.getFormattedDbDate(LocalDate.now()) + "', 'yyyy-mm-dd'))";
		}
		sql += " ORDER BY entryType.description";
		
		return entityManager.createQuery(sql).getResultList();
	}	
	
	@SuppressWarnings("unchecked")
	public List<Expense> getAll() {
		Query query = entityManager.createQuery(
				"from Expense ORDER BY dueDate DESC,entryType");
		query.setMaxResults(Statics.MAX_RESULTS.getIntValue());

		return query.getResultList();
	}	
	
	@SuppressWarnings("unchecked")
	public List<Expense> findAll() {
		Query query = entityManager.createQuery("from Expense");

		return query.getResultList();
	}	
	
	public Expense getById(long id) {
		return entityManager.find(Expense.class, id);
	}
	
	public Expense update(Expense expense) {
		return entityManager.merge(expense);
	}

	public List<Expense> getForWeek(LocalDate weekStartDate) {
		String sql = "from Expense where recurringType IS NULL "
				+ "AND dueDate >= to_date('" + DateUtil.getFormattedDbDate(weekStartDate) + "', 'yyyy-mm-dd') "
				+ "AND dueDate <= to_date('" + DateUtil.getFormattedDbDate(weekStartDate.plusDays(6)) + "', 'yyyy-mm-dd')"
				+ " ORDER BY dueDate,entryType";

		return entityManager.createQuery(sql).getResultList();
	}
	
	public List<Expense> getUnpaidBeforeWeek(LocalDate weekStartDate) {
		String sql = "from Expense where recurringType IS NULL "
				+ "AND dueDate < to_date('" + DateUtil.getFormattedDbDate(weekStartDate) + "', 'yyyy-mm-dd') "
				+ "AND paid = false ORDER BY dueDate,entryType";

		return entityManager.createQuery(sql).getResultList();
	}	
	
	public List<Expense> getPastDate(LocalDate date) {
		String sql = "from Expense where recurringType IS NULL"
				+ " AND dueDate > to_date('" + DateUtil.getFormattedDbDate(date) + "', 'yyyy-mm-dd')";

		return entityManager.createQuery(sql).getResultList();
	}

	public List<Expense> getPastDate(LocalDate date, Expense recurringExpense) {
		String sql = "from Expense where dueDate > to_date('" + DateUtil.getFormattedDbDate(date)
				+ "', 'yyyy-mm-dd') and recurringTransaction = :recurringTransaction";
		Query query = entityManager.createQuery(sql);
		query.setParameter("recurringTransaction", recurringExpense);

		return query.getResultList();
	}
	
	public List<Expense> findExpenses(SearchParamsDto searchParamsDto) {
		String sql = "SELECT * FROM transactions e LEFT JOIN refdata r on e.entrytypeId = r.id "
				+ "where transactiontype = 'EXPENSE' AND e.recurringtypeid IS NULL ";
		if (!StringUtils.isEmpty(searchParamsDto.getTransactionType())) {
			sql += "AND lower(r.description) = lower('" + searchParamsDto.getTransactionType().getDescription() + "') ";
		}
		if (!StringUtils.isEmpty(searchParamsDto.getKeyWords())) {
			sql += "AND lower(e.notes) LIKE lower('%" + searchParamsDto.getKeyWords() + "%') ";
		}
		if (!StringUtils.isEmpty(searchParamsDto.getStartDateString())) {
			sql += "AND e.dueDate >= to_date('" + DateUtil.getFormattedDbDate(searchParamsDto.getStartDateString()) + "', 'yyyy-mm-dd') ";
		}
		if (!StringUtils.isEmpty(searchParamsDto.getEndDateString())) {
			sql += "AND e.dueDate <= to_date('" + DateUtil.getFormattedDbDate(searchParamsDto.getEndDateString()) + "', 'yyyy-mm-dd') ";
		}
		sql += "ORDER BY e.dueDate DESC,r.description";
		Query query = entityManager.createNativeQuery(sql, Expense.class);
		List<Expense> results = query.getResultList();
		if (!StringUtils.isEmpty(searchParamsDto.getMetaDataChunk())) {
			return filterByMetadata(searchParamsDto, results);
		}
		
		return results.stream().limit(Statics.MAX_RESULTS.getIntValue()).collect(Collectors.toList());
		
	}

	public List<Expense> getForRecurring(Expense recurringExpense) {
		String sql = "from Expense where recurringTransaction = :recurringTransaction";
		Query query = entityManager.createQuery(sql);
		query.setParameter("recurringTransaction", recurringExpense);

		return query.getResultList();
	}

	public void deleteTransactions(Long recurringTransactionId) {
		entityManager.createQuery("delete from Expense where recurringTransaction.id = " + recurringTransactionId
				+ " AND dueDate > to_date('" + DateUtil.getFormattedDbDate(LocalDate.now()) + "', 'yyyy-mm-dd')")
				.executeUpdate();
	}
	
	public List<Expense> findExpenses(RefData entryType) {
		String sql = "from Expense where entryType = :entryType AND paid = false AND recurringType is null order by dueDate";
		Query query = entityManager.createQuery(sql);
		query.setParameter("entryType", entryType);

		return query.getResultList();
	}
	
	// Private fields

	// An EntityManager will be automatically injected from entityManagerFactory
	// setup on DatabaseConfig class.
	@PersistenceContext
	private EntityManager entityManager;

}
