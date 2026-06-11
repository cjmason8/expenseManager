package au.com.mason.expensemanager.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("INCOME")
@Getter
@Setter
@NoArgsConstructor
public class Income extends Transaction {

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
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
