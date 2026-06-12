package au.com.mason.expensemanager.domain;

import jakarta.persistence.Convert;
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
import au.com.mason.expensemanager.domain.converter.TimestampLocalDateConverter;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NamedQueries(
		value = {
				@NamedQuery(
						name = Notification.GET_NOT_REMOVED,
						query = "FROM Notification where removed = false"),
				@NamedQuery(
						name = Notification.FIND_FOR_EXPENSE,
						query = "FROM Notification where expense = :expense"),
		})
@Entity
@Table(name="notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

	public static final String GET_NOT_REMOVED = "Notification.Repository.GetNotRemoved";
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

	@Convert(converter = TimestampLocalDateConverter.class)
	private LocalDate created = LocalDate.now();

	private boolean removed = false;

}
