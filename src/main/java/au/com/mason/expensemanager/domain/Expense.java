package au.com.mason.expensemanager.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

@NamedQueries(
		value = {
				@NamedQuery(
						name = Expense.GET_ALL_RECURRING,
						query = "from Expense where recurringType IS NOT NULL AND deleted = false ORDER BY entryType.description"),
				@NamedQuery(
						name = Expense.GET_RECURRING,
						query = "from Expense where recurringType IS NOT NULL AND deleted = false AND (endDate is NULL OR endDate >= to_date(:endDate, 'yyyy-mm-dd')) ORDER BY entryType.description"),
				@NamedQuery(
						name = Expense.GET_ALL,
						query = "from Expense ORDER BY dueDate DESC,entryType.type"),
				@NamedQuery(
						name = Expense.GET_FOR_WEEK,
						query = "from Expense where recurringType IS NULL AND dueDate >= to_date(:weekStartDate, 'yyyy-mm-dd') AND dueDate <= to_date(:weekLaterFromStartDate, 'yyyy-mm-dd') ORDER BY dueDate,entryType.type"),
				@NamedQuery(
						name = Expense.GET_UNPAID_BEFORE_WEEK,
						query = "from Expense where recurringType IS NULL AND dueDate < to_date(:weekStartDate, 'yyyy-mm-dd') AND paid = false ORDER BY dueDate,entryType.type"),
				@NamedQuery(
						name = Expense.GET_PAST_DATE,
						query = "from Expense where recurringType IS NULL AND dueDate > to_date(:date, 'yyyy-mm-dd')"),
		})
@Entity
@DiscriminatorValue("EXPENSE")
public class Expense extends Transaction {

	public static final String GET_ALL_RECURRING = "Expense.Repository.GetAllRecurring";
	public static final String GET_RECURRING = "Expense.Repository.GetRecurring";
	public static final String GET_ALL = "Expense.Repository.GetAll";
	public static final String GET_FOR_WEEK = "Expense.Repository.GetForWeek";
	public static final String GET_UNPAID_BEFORE_WEEK = "Expense.Repository.GetUnpaidBeforeWeek";
	public static final String GET_PAST_DATE = "Expense.Repository.GetPastDate";
	
	@OneToOne
	@JoinColumn(name = "recurringTransactionId")
	private Expense recurringTransaction;
	
	private boolean paid = false;

	@Override
	public Expense getRecurringTransaction() {
		return recurringTransaction;
	}

	@Override
	public void setRecurringTransaction(Transaction recurringTransaction) {
		this.recurringTransaction = (Expense) recurringTransaction;
	}
	
	public boolean getPaid() {
		return paid;
	}

	public void setPaid(boolean paid) {
		this.paid = paid;
	}

}
