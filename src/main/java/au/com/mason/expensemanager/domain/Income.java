package au.com.mason.expensemanager.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
@DiscriminatorValue("INCOME")
public class Income extends Transaction {
	
	@OneToOne
	@JoinColumn(name = "recurringTransactionId")
	private Income recurringTransaction;
	
	@Override
	public Transaction getRecurringTransaction() {
		return recurringTransaction;
	}

	@Override
	public void setRecurringTransaction(Transaction recurringTransaction) {
		this.recurringTransaction = (Income) recurringTransaction;
	}

}
