package au.com.mason.expensemanager.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.domain.Notification;

@Repository
@Transactional
public class NotificationDao extends BaseDao<Notification> {

	public NotificationDao(@Qualifier("entityManagerFactory") EntityManager entityManager) {
		super(Notification.class, entityManager);
	}

	public List<Notification> getUnread() {
		Query query = entityManager.createNamedQuery(Notification.GET_UNREAD, Notification.class);

		return query.getResultList();
	}
	
	public void deleteForExpense(Expense expense) {
		Query query = entityManager.createNamedQuery(Notification.FIND_FOR_EXPENSE, Notification.class);
		query.setParameter("expense", expense);

		List<Notification> notifications = query.getResultList();
		
		notifications.forEach(notification -> entityManager.remove(notification));
	}
	
}
