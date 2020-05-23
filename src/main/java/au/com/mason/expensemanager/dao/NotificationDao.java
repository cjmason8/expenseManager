package au.com.mason.expensemanager.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.domain.Notification;

@Repository
@Transactional
public class NotificationDao {
	
	public List<Notification> getUnread() {
		Query query = entityManager.createQuery("FROM Notification where read = false");

		return query.getResultList();
	}
	
	public Notification create(Notification notification) {
		entityManager.persist(notification);

		return notification;
	}
	
	public Notification getById(long id) {
		return entityManager.find(Notification.class, id);
	}
	
	public Notification update(Notification notification) {
		return entityManager.merge(notification);
	}
	
	public void deleteForExpense(Expense expense) {
		Query query = entityManager.createQuery("FROM Notification where expense = :expense");
		query.setParameter("expense", expense);

		List<Notification> notifications = query.getResultList();
		
		notifications.forEach(notification -> entityManager.remove(notification));
	}
	
	// Private fields

	// An EntityManager will be automatically injected from entityManagerFactory
	// setup on DatabaseConfig class.
	@PersistenceContext
	private EntityManager entityManager;

}
