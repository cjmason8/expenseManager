package au.com.mason.expensemanager.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.domain.Notification;

@Repository
@Transactional
public abstract class NotificationRepository extends BaseRepository<Notification> {

	protected NotificationRepository(EntityManager em) {
		super(em, Notification.class);
	}

	@Query(" SELECT O FROM Notification O WHERE read = false")
	public List<Notification> getUnread() {
		Query query = em.createNamedQuery(Notification.GET_UNREAD, Notification.class);

		return query.getResultList();
	}
	
	public void deleteForExpense(Expense expense) {
		Query query = em.createNamedQuery(Notification.GET_FOR_EXPENSE, Notification.class);
		query.setParameter("expense", expense);

		List<Notification> notifications = query.getResultList();
		
		notifications.forEach(notification -> em.remove(notification));
	}
}
