package au.com.mason.expensemanager.dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

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

	public List<Notification> getNotRemoved() {
		Query query = entityManager.createNamedQuery(Notification.GET_NOT_REMOVED, Notification.class);

		return query.getResultList();
	}

	public List<Notification> getAll() {
		Query query = entityManager.createNamedQuery(Notification.GET_ALL, Notification.class);

		return query.getResultList();
	}

	public void deleteForExpense(Expense expense) {
		Query query = entityManager.createNamedQuery(Notification.FIND_FOR_EXPENSE, Notification.class);
		query.setParameter("expense", expense);

		List<Notification> notifications = query.getResultList();

		notifications.forEach(notification -> entityManager.remove(notification));
	}

}
