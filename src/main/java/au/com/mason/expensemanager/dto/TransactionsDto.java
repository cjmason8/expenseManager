package au.com.mason.expensemanager.dto;

import java.util.List;

public class TransactionsDto {

	private List<ExpenseDto> expenses;
	private List<IncomeDto> incomes;

	public TransactionsDto(List<IncomeDto> incomes, List<ExpenseDto> expenses) {
		this.expenses = expenses;
		this.incomes = incomes;
	}

	public List<ExpenseDto> getExpenses() {
		return expenses;
	}

	public List<IncomeDto> getIncomes() {
		return incomes;
	}

}
