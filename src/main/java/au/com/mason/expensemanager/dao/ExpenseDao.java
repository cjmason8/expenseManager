package au.com.mason.expensemanager.dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import au.com.mason.expensemanager.domain.EntityMetadataType;
import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.domain.Statics;
import au.com.mason.expensemanager.dto.RefDataDto;
import au.com.mason.expensemanager.dto.SearchParamsDto;
import au.com.mason.expensemanager.service.EntityMetadataService;
import au.com.mason.expensemanager.util.DateUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Repository
@Transactional
public class ExpenseDao extends BaseDao<Expense> implements TransactionDao<Expense> {

	private final Gson gson = new GsonBuilder().serializeNulls().create();

	@Autowired
	private NotificationDao notificationDao;

	@Autowired
	private EntityMetadataService entityMetadataService;

	public ExpenseDao(@Qualifier("entityManagerFactory") EntityManager entityManager) {
		super(Expense.class, entityManager);
	}

	@Override
	public Expense getById(long id) {
		return super.getById(Long.valueOf(id));
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
		Query query = entityManager
			.createQuery("from Expense where dueDate > :date and recurringTransaction = :recurringTransaction");
		query.setParameter("date", date);
		query.setParameter("recurringTransaction", recurringExpense);

		return query.getResultList();
	}

	public List<Expense> findExpenses(SearchParamsDto searchParamsDto) {
		StringBuilder jpql = new StringBuilder("SELECT e FROM Expense e WHERE e.recurringType IS NULL ");
		if (searchParamsDto.getTransactionType() != null) {
			RefDataDto tt = searchParamsDto.getTransactionType();
			if (tt.getId() != null) {
				jpql.append("AND e.entryType.id = :entryTypeId ");
			} else if (tt.getDescription() != null) {
				jpql.append("AND lower(e.entryType.description) = lower(:entryTypeDescription) ");
			}
		}
		if (!StringUtils.isEmpty(searchParamsDto.getKeyWords())) {
			jpql.append("AND lower(e.notes) LIKE lower(:keyWords) ");
		}
		if (!StringUtils.isEmpty(searchParamsDto.getStartDateString())) {
			jpql.append("AND e.dueDate >= :startDate ");
		}
		if (!StringUtils.isEmpty(searchParamsDto.getEndDateString())) {
			jpql.append("AND e.dueDate <= :endDate ");
		}
		jpql.append("ORDER BY e.dueDate DESC, e.entryType.description");

		TypedQuery<Expense> query = entityManager.createQuery(jpql.toString(), Expense.class);
		if (searchParamsDto.getTransactionType() != null) {
			RefDataDto tt = searchParamsDto.getTransactionType();
			if (tt.getId() != null) {
				query.setParameter("entryTypeId", tt.getId());
			} else if (tt.getDescription() != null) {
				query.setParameter("entryTypeDescription", tt.getDescription());
			}
		}
		if (!StringUtils.isEmpty(searchParamsDto.getKeyWords())) {
			query.setParameter("keyWords", "%" + searchParamsDto.getKeyWords() + "%");
		}
		if (!StringUtils.isEmpty(searchParamsDto.getStartDateString())) {
			query.setParameter("startDate", DateUtil.getFormattedDate(searchParamsDto.getStartDateString()));
		}
		if (!StringUtils.isEmpty(searchParamsDto.getEndDateString())) {
			query.setParameter("endDate", DateUtil.getFormattedDate(searchParamsDto.getEndDateString()));
		}

		List<Expense> results = query.getResultList();
		hydrateExpenses(results);
		if (!StringUtils.isEmpty(searchParamsDto.getMetaDataChunk())) {
			return filterByMetadata(searchParamsDto.getMetaDataChunk(), results);
		}

		return results.stream().limit(Statics.MAX_RESULTS.getIntValue()).collect(Collectors.toList());
	}

	private void hydrateExpenses(List<Expense> expenses) {
		entityMetadataService.hydrateList(EntityMetadataType.EXPENSE, expenses, e -> String.valueOf(e.getId()),
			(entity, entityMetadata, objectMap, stringMap) -> {
				entity.setEntityMetadata(entityMetadata);
				entity.setMetaData(objectMap);
			});
	}

	@SuppressWarnings("unchecked")
	private List<Expense> filterByMetadata(String metaDataChunk, List<Expense> results) {
		Map<String, Object> criteria = gson.fromJson(metaDataChunk, Map.class);
		if (criteria == null || criteria.isEmpty()) {
			return results;
		}
		List<Expense> filtered = new ArrayList<>();
		for (Expense expense : results) {
			Map<String, Object> metaData = expense.getMetaData();
			if (metaData == null) {
				continue;
			}
			boolean matches = true;
			for (Map.Entry<String, Object> entry : criteria.entrySet()) {
				Object stored = metaData.get(entry.getKey());
				if (!valueMatches(stored, entry.getValue())) {
					matches = false;
					break;
				}
			}
			if (matches) {
				filtered.add(expense);
			}
		}
		return filtered;
	}

	private boolean valueMatches(Object stored, Object criteria) {
		if (stored == null) {
			return false;
		}
		if (criteria instanceof Collection<?> criteriaList) {
			for (Object item : criteriaList) {
				if (valueEqualsIgnoreCase(stored, item)) {
					return true;
				}
			}
			return false;
		}
		return valueEqualsIgnoreCase(stored, criteria);
	}

	private boolean valueEqualsIgnoreCase(Object stored, Object criteria) {
		if (stored instanceof Collection<?> storedList) {
			return storedList.stream().anyMatch(v -> Objects.equals(toLower(v), toLower(criteria)));
		}
		return Objects.equals(toLower(stored), toLower(criteria));
	}

	private String toLower(Object val) {
		return val == null ? null : String.valueOf(val).toLowerCase();
	}

	public List<Expense> getForRecurring(Expense recurringExpense) {
		Query query = entityManager.createQuery("from Expense where recurringTransaction = :recurringTransaction");
		query.setParameter("recurringTransaction", recurringExpense);

		return query.getResultList();
	}

	public void deleteTransactions(Long recurringTransactionId) {
		entityManager
			.createQuery(
				"delete from Expense where recurringTransaction.id = :recurringTransactionId AND dueDate > :today")
			.setParameter("recurringTransactionId", recurringTransactionId).setParameter("today", LocalDate.now())
			.executeUpdate();
	}

	public List<Expense> findExpenses(RefData entryType) {
		Query query = entityManager.createQuery(
			"from Expense where entryType = :entryType AND paid = false AND recurringType is null order by dueDate");
		query.setParameter("entryType", entryType);

		return query.getResultList();
	}

}
