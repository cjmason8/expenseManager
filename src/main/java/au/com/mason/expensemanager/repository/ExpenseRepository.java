package au.com.mason.expensemanager.repository;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.util.DateUtil;

@Repository
@Transactional
public interface ExpenseRepository extends CrudRepository<Expense, Long>, TransactionRepository<Expense> {
	
	List<Expense> findByRecurringTransaction(Expense recurringTransaction);

	//DateUtil.getFormattedDbDate(LocalDate.now())
	@Query("DELETE FROM Expense WHERE recurringTransaction.id = :recurringTransactionId AND dueDate > to_date(':requiredDueDate', 'yyyy-mm-dd')")
	void deleteTransactions(Long recurringTransactionId, String requiredDueDate);
	
	@Query("SELECT a FROM Expense a WHERE a.entryType = :entryType AND a.paid = false AND a.recurringType IS NULL ORDER BY a.dueDate")
	List<Expense> findExpenses(RefData entryType);
	
}
