package au.com.mason.expensemanager.dao;

import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.domain.Statics;
import au.com.mason.expensemanager.dto.SearchParamsDto;
import au.com.mason.expensemanager.util.DateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class ExpenseDao extends MetaDataDao<Expense> implements TransactionDao<Expense> {

	@Autowired
	private NotificationDao notificationDao;

	public ExpenseDao(@Qualifier("entityManagerFactory") EntityManager entityManager) {
		super(Expense.class, entityManager);
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
		Query query = entityManager.createNamedQuery(Expense.GET_ALL_RECURRING, Expense.class);
		if (!includeAll) {
			query = entityManager.createNamedQuery(Expense.GET_RECURRING, Expense.class);
			query.setParameter("endDate", LocalDate.now());
		}

		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Expense> getAll() {
		Query query = entityManager.createNamedQuery(Expense.GET_ALL, Expense.class);
		query.setMaxResults(Statics.MAX_RESULTS.getIntValue());

		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Expense> getForWeek(LocalDate weekStartDate) {
		Query query = entityManager.createNamedQuery(Expense.GET_FOR_WEEK, Expense.class);
		query.setParameter("weekStartDate", weekStartDate);
		query.setParameter("weekLaterFromStartDate", weekStartDate.plusDays(6));

		return query.getResultList();
	}

	public List<Expense> getUnpaidBeforeWeek(LocalDate weekStartDate) {
		Query query = entityManager.createNamedQuery(Expense.GET_UNPAID_BEFORE_WEEK);
		query.setParameter("weekStartDate", weekStartDate);

		return query.getResultList();
	}

	public List<Expense> getPastDate(LocalDate date) {
		Query query = entityManager.createNamedQuery(Expense.GET_PAST_DATE);
		query.setParameter("date", date);

		return query.getResultList();
	}

	public List<Expense> getPastDate(LocalDate date, Expense recurringExpense) {
		Query query = entityManager.createQuery(
				"from Expense where dueDate > :date and recurringTransaction = :recurringTransaction");
		query.setParameter("date", date);
		query.setParameter("recurringTransaction", recurringExpense);

		return query.getResultList();
	}

	public List<Expense> findExpenses(SearchParamsDto searchParamsDto) {
		String sql = "SELECT e.* FROM transactions e LEFT JOIN refdata r on e.entrytypeId = r.id "
				+ "where transactiontype = 'EXPENSE' AND e.recurringtypeid IS NULL ";
		Map<String, Object> parameters = new HashMap<>();
		if (searchParamsDto.getTransactionType() != null) {
			sql += "AND lower(r.description) = lower(:transactionTypeDescription) ";
			parameters.put("transactionTypeDescription", searchParamsDto.getTransactionType().getDescription());
		}
		if (!StringUtils.isEmpty(searchParamsDto.getKeyWords())) {
			sql += "AND lower(e.notes) LIKE lower(:keyWords) ";
			parameters.put("keyWords", "%" + searchParamsDto.getKeyWords() + "%");
		}
		if (!StringUtils.isEmpty(searchParamsDto.getStartDateString())) {
			sql += "AND e.duedate >= :startDate ";
			parameters.put("startDate", DateUtil.getFormattedDate(searchParamsDto.getStartDateString()));
		}
		if (!StringUtils.isEmpty(searchParamsDto.getEndDateString())) {
			sql += "AND e.duedate <= :endDate ";
			parameters.put("endDate", DateUtil.getFormattedDate(searchParamsDto.getEndDateString()));
		}
		sql += "ORDER BY e.duedate DESC,r.description";
		Query query = entityManager.createNativeQuery(sql, Expense.class);
		parameters.forEach(query::setParameter);
		List<Expense> results = query.getResultList();
		if (!StringUtils.isEmpty(searchParamsDto.getMetaDataChunk())) {
			return filterByMetadata(searchParamsDto, results);
		}

		return results.stream().limit(Statics.MAX_RESULTS.getIntValue()).collect(Collectors.toList());

	}

	public List<Expense> getForRecurring(Expense recurringExpense) {
		Query query = entityManager.createQuery("from Expense where recurringTransaction = :recurringTransaction");
		query.setParameter("recurringTransaction", recurringExpense);

		return query.getResultList();
	}

	public void deleteTransactions(Long recurringTransactionId) {
		entityManager.createQuery(
				"delete from Expense where recurringTransaction.id = :recurringTransactionId AND dueDate > :today")
				.setParameter("recurringTransactionId", recurringTransactionId)
				.setParameter("today", LocalDate.now())
				.executeUpdate();
	}

	public List<Expense> findExpenses(RefData entryType) {
		Query query = entityManager.createQuery(
				"from Expense where entryType = :entryType AND paid = false AND recurringType is null order by dueDate");
		query.setParameter("entryType", entryType);

		return query.getResultList();
	}

}
