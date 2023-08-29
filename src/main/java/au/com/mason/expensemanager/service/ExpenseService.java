package au.com.mason.expensemanager.service;

import au.com.mason.expensemanager.domain.Statics;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.repository.ExpenseRepository;
import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.domain.Transaction;

@Component
public class ExpenseService extends TransactionService<Expense> {

	@Autowired
	private IncomeService incomeService;
	
	@Autowired
	private ExpenseRepository expenseRepository;

	protected ExpenseService() {
		super(Expense.class, expenseRepository);
	}

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
		return expenseRepository.getUnpaidBeforeWeek(startOfWeek);
	}

	@Override
	int countForWeekForAll(LocalDate startOfWeek) throws Exception {
		return countForWeek(startOfWeek) + incomeService.countForWeek(startOfWeek);
	}
	
	public Expense payExpense(Long id) {
		Expense expense = expenseRepository.findById(id);
		expense.setPaid(true);
		
		expenseRepository.save(expense);
		
		return expense;
	}
	
	public Expense unPayExpense(Long id) throws Exception {
		Expense expense = expenseRepository.getById(id);
		expense.setPaid(false);
		
		expenseRepository.save(expense);
		
		return expense;
	}
	
	public List<Expense> getAll() throws Exception {
		var results = new ArrayList<Expense>();
		expenseRepository.findAll().forEach(results::add);
		results.sort(Comparator.comparing(Expense::getDueDate).reversed().thenComparing(e -> e.getEntryType().getType()));

		return results.stream().limit(Statics.MAX_RESULTS.getIntValue()).collect(Collectors.toList());
	}
	
	public List<Expense> findExpense(RefData entryType) {
		return expenseRepository.findExpenses(entryType);
	}
	
}
