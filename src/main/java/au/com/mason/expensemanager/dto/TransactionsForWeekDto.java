package au.com.mason.expensemanager.dto;

import java.math.BigDecimal;
import java.util.List;

public class TransactionsForWeekDto {

	private List<ExpenseDto> expenses;
	private List<ExpenseDto> unpaidExpenses;
	private List<IncomeDto> incomes;
	private String previousWeek;
	private String nextWeek;
	private String thisWeek;
	private BigDecimal incomeTotal = new BigDecimal(0);
	private BigDecimal expenseTotal = new BigDecimal(0);
	private BigDecimal unpaidExpenseTotal = new BigDecimal(0);
	private BigDecimal difference = new BigDecimal(0);

	public void setTotals() {
		for (IncomeDto income : this.incomes) {
			incomeTotal = incomeTotal.add(new BigDecimal(income.getAmount()));
		}

		for (ExpenseDto expense : this.expenses) {
			expenseTotal = expenseTotal.add(new BigDecimal(expense.getAmount()));
		}

		if (this.unpaidExpenses != null) {
			for (ExpenseDto unpaidExpense : this.unpaidExpenses) {
				unpaidExpenseTotal = unpaidExpenseTotal.add(new BigDecimal(unpaidExpense.getAmount()));
			}
		}

		difference = incomeTotal.subtract(expenseTotal);
	}

	public List<ExpenseDto> getExpenses() {
		return expenses;
	}

	public List<IncomeDto> getIncomes() {
		return incomes;
	}

	public String getPreviousWeek() {
		return previousWeek;
	}

	public String getNextWeek() {
		return nextWeek;
	}

	public String getThisWeek() {
		return thisWeek;
	}

	public BigDecimal getIncomeTotal() {
		return incomeTotal;
	}

	public void setIncomeTotal(BigDecimal incomeTotal) {
		this.incomeTotal = incomeTotal;
	}

	public BigDecimal getExpenseTotal() {
		return expenseTotal;
	}

	public void setExpenseTotal(BigDecimal expenseTotal) {
		this.expenseTotal = expenseTotal;
	}

	public BigDecimal getDifference() {
		return difference;
	}

	public void setDifference(BigDecimal difference) {
		this.difference = difference;
	}

	public List<ExpenseDto> getUnpaidExpenses() {
		return unpaidExpenses;
	}

	public void setUnpaidExpenses(List<ExpenseDto> unpaidExpenses) {
		this.unpaidExpenses = unpaidExpenses;
	}

	public void setExpenses(List<ExpenseDto> expenses) {
		this.expenses = expenses;
	}

	public void setIncomes(List<IncomeDto> incomes) {
		this.incomes = incomes;
	}

	public void setPreviousWeek(String previousWeek) {
		this.previousWeek = previousWeek;
	}

	public void setNextWeek(String nextWeek) {
		this.nextWeek = nextWeek;
	}

	public void setThisWeek(String thisWeek) {
		this.thisWeek = thisWeek;
	}

	public BigDecimal getUnpaidExpenseTotal() {
		return unpaidExpenseTotal;
	}

	public void setUnpaidExpenseTotal(BigDecimal unpaidExpenseTotal) {
		this.unpaidExpenseTotal = unpaidExpenseTotal;
	}

}
