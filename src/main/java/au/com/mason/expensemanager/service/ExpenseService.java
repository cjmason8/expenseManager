package au.com.mason.expensemanager.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.dao.ExpenseDao;
import au.com.mason.expensemanager.dao.TransactionDao;
import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.domain.Transaction;

@Component
public class ExpenseService extends TransactionService<Expense, ExpenseDao> {

	@Autowired
	private IncomeService incomeService;

	@Autowired
	private TransactionDao<Expense> expenseDao;

	@Override
	Expense createTransaction() {
		return new Expense();
	}

	@Override
	public void initialiseWeek(LocalDate localDate, Transaction currentRecurringTransaction) throws Exception {
		if (countForWeekForAll(localDate) == 0) {
			incomeService.createRecurringTransactions(localDate, currentRecurringTransaction);
			createRecurringTransactions(localDate, currentRecurringTransaction);
		}
	}

	@Override
	public int getPastDate(LocalDate startOfWeek) throws Exception {
		return getPastDateList(startOfWeek).size() + incomeService.getPastDateList(startOfWeek).size();
	}

	public List<Expense> getUnpaidBeforeWeek(LocalDate startOfWeek) throws Exception {
		List<Expense> results = expenseDao.getUnpaidBeforeWeek(startOfWeek);
		hydrateTransactions(results);
		return results;
	}

	@Override
	int countForWeekForAll(LocalDate startOfWeek) throws Exception {
		return countForWeek(startOfWeek) + incomeService.countForWeek(startOfWeek);
	}

	public Expense payExpense(Long id) throws Exception {
		Expense expense = expenseDao.getById(id);
		expense.setPaid(true);

		expenseDao.update(expense);
		hydrateTransaction(expense);
		return expense;
	}

	public Expense unPayExpense(Long id) throws Exception {
		Expense expense = expenseDao.getById(id);
		expense.setPaid(false);

		expenseDao.update(expense);
		hydrateTransaction(expense);
		return expense;
	}

	public List<Expense> getAll() throws Exception {
		List<Expense> results = expenseDao.getAll();
		hydrateTransactions(results);
		return results;
	}

	public List<Expense> findExpense(RefData entryType) {
		List<Expense> results = expenseDao.findExpenses(entryType);
		hydrateTransactions(results);
		return results;
	}

}
