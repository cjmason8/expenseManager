package au.com.mason.expensemanager.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Date;

@NamedQueries(
		value = {
				@NamedQuery(
						name = Notification.GET_UNREAD,
						query = "FROM Notification where read = false"),
				@NamedQuery(
						name = Notification.FIND_FOR_EXPENSE,
						query = "FROM Notification where expense = :expense"),
		})
@Entity
@Table(name="notifications")
public class Notification {

	public static final String GET_UNREAD = "Notification.Repository.GetUnread";
	public static final String FIND_FOR_EXPENSE = "Notification.Repository.FindForExpense";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "notifications_seq")
	@SequenceGenerator(name = "notifications_seq", sequenceName = "notifications_seq", allocationSize = 1)
	private long id;
	
	@OneToOne
	@JoinColumn(name = "expenseId")
	private Expense expense;

	private String message;
	
	private boolean read = false;
	
	private Date created = new Date();

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Expense getExpense() {
		return expense;
	}

	public void setExpense(Expense expense) {
		this.expense = expense;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public LocalDate getCreated() {
		if (created != null) {
			return new java.sql.Date(created.getTime()).toLocalDate();
		}
	
		return null;
	}

	public void setCreated(LocalDate created) {
		this.created = java.sql.Date.valueOf(created);
	}
	
}
