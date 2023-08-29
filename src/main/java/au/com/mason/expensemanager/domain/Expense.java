package au.com.mason.expensemanager.domain;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
@DiscriminatorValue("EXPENSE")
public class Expense extends Transaction {
	
	@OneToOne
	@JoinColumn(name = "recurringTransactionId")
	private Expense recurringTransaction;
	
	private boolean paid = false;

	@OneToMany(mappedBy = "notification", cascade = CascadeType.ALL)
	private List<Notification> notifications;

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
