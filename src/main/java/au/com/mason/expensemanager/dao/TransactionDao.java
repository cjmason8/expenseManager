package au.com.mason.expensemanager.dao;

import java.time.LocalDate;
import java.util.List;

import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.dto.SearchParamsDto;

public interface TransactionDao<T> {
	
	public T create(T transation);	
	public void delete(T transation);
	public List<T> getAllRecurring(boolean includeAll);
	public T getById(long id);
	public T update(T transation);
	public List<T> getForWeek(LocalDate weekStartDate);
	public List<T> getPastDate(LocalDate date);
	public List<T> getPastDate(LocalDate date, T recurringTransation);
	public List<T> getForRecurring(T recurringTransation);
	public void deleteTransactions(Long recurringTransactionId);
	
	default List<T> getUnpaidBeforeWeek(LocalDate weekStartDate) {
		// TODO Auto-generated method stub
		return null;
	}

	default List<T> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

	default List<T> findExpenses(SearchParamsDto searchParamsDto) {
		// TODO Auto-generated method stub
		return null;
	}

	default List<T> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	default List<Expense> findExpenses(RefData entryType) {
		// TODO Auto-generated method stub
		return null;
	}
}
