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
import au.com.mason.expensemanager.domain.Income;
import au.com.mason.expensemanager.domain.Statics;
import au.com.mason.expensemanager.dto.RefDataDto;
import au.com.mason.expensemanager.dto.SearchParamsDto;
import au.com.mason.expensemanager.service.EntityMetadataService;
import au.com.mason.expensemanager.util.DateUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Repository
@Transactional
public class IncomeDao extends BaseDao<Income> implements TransactionDao<Income> {

	private final Gson gson = new GsonBuilder().serializeNulls().create();

	@Autowired
	private EntityMetadataService entityMetadataService;

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
		Query query = entityManager.createQuery("from Income where recurringType IS NULL AND dueDate >= :weekStartDate "
			+ "AND dueDate <= :weekEndDate ORDER BY dueDate,entryType.type");
		query.setParameter("weekStartDate", weekStartDate);
		query.setParameter("weekEndDate", weekStartDate.plusDays(6));

		return query.getResultList();
	}

	public List<Income> getPastDate(LocalDate date) {
		Query query = entityManager.createQuery("from Income where recurringType IS NULL AND dueDate > :date");
		query.setParameter("date", date);

		return query.getResultList();
	}

	public List<Income> getPastDate(LocalDate date, Income recurringIncome) {
		Query query = entityManager
			.createQuery("from Income where dueDate > :date and recurringTransaction = :recurringTransaction");
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
		entityManager
			.createQuery(
				"delete from Income where recurringTransaction.id = :recurringTransactionId AND dueDate > :today")
			.setParameter("recurringTransactionId", recurringTransactionId).setParameter("today", LocalDate.now())
			.executeUpdate();
	}

	public List<Income> findIncomes(SearchParamsDto searchParamsDto) {
		StringBuilder jpql = new StringBuilder("SELECT i FROM Income i WHERE i.recurringType IS NULL ");
		if (searchParamsDto.getTransactionType() != null) {
			RefDataDto tt = searchParamsDto.getTransactionType();
			if (tt.getId() != null) {
				jpql.append("AND i.entryType.id = :entryTypeId ");
			} else if (tt.getDescription() != null) {
				jpql.append("AND lower(i.entryType.description) = lower(:entryTypeDescription) ");
			}
		}
		if (!StringUtils.isEmpty(searchParamsDto.getKeyWords())) {
			jpql.append("AND lower(i.notes) LIKE lower(:keyWords) ");
		}
		if (!StringUtils.isEmpty(searchParamsDto.getStartDateString())) {
			jpql.append("AND i.dueDate >= :startDate ");
		}
		if (!StringUtils.isEmpty(searchParamsDto.getEndDateString())) {
			jpql.append("AND i.dueDate <= :endDate ");
		}
		jpql.append("ORDER BY i.dueDate DESC, i.entryType.description");

		TypedQuery<Income> query = entityManager.createQuery(jpql.toString(), Income.class);
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

		List<Income> results = query.getResultList();
		hydrateIncomes(results);
		if (!StringUtils.isEmpty(searchParamsDto.getMetaDataChunk())) {
			return filterByMetadata(searchParamsDto.getMetaDataChunk(), results);
		}

		return results.stream().limit(Statics.MAX_RESULTS.getIntValue()).collect(Collectors.toList());
	}

	private void hydrateIncomes(List<Income> incomes) {
		entityMetadataService.hydrateList(EntityMetadataType.INCOME, incomes, i -> String.valueOf(i.getId()),
			(entity, entityMetadata, objectMap, stringMap) -> {
				entity.setEntityMetadata(entityMetadata);
				entity.setMetaData(objectMap);
			});
	}

	@SuppressWarnings("unchecked")
	private List<Income> filterByMetadata(String metaDataChunk, List<Income> results) {
		Map<String, Object> criteria = gson.fromJson(metaDataChunk, Map.class);
		if (criteria == null || criteria.isEmpty()) {
			return results;
		}
		List<Income> filtered = new ArrayList<>();
		for (Income income : results) {
			Map<String, Object> metaData = income.getMetaData();
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
				filtered.add(income);
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

}
