package au.com.mason.expensemanager.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.dao.IncomeDao;
import au.com.mason.expensemanager.domain.Income;
import au.com.mason.expensemanager.domain.Transaction;
import au.com.mason.expensemanager.dto.IncomeDto;

@Component
public class IncomeService extends TransactionService<IncomeDto, Income, IncomeDao> {
	
	@Autowired
	private ExpenseService expenseService;
	
	@Override
	Income createTransaction() {
		return new Income();
	}

	@Override
	void initialiseWeek(LocalDate localDate, Transaction currentRecurringTransaction) throws Exception {
		expenseService.initialiseWeek(localDate, currentRecurringTransaction);
	}
	
	@Override
	public int getPastDate(LocalDate startOfWeek) throws Exception {
		return getPastDateList(startOfWeek).size() + expenseService.getPastDateList(startOfWeek).size();
	}
	
	@Override
	int countForWeekForAll(LocalDate startOfWeek) throws Exception {
		return countForWeek(startOfWeek) + expenseService.countForWeek(startOfWeek);
	}

	@Override
	void initialiseWeek(LocalDate localDate) throws Exception {
		expenseService.initialiseWeek(localDate);
	}
	
}
