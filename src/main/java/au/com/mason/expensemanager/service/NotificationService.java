package au.com.mason.expensemanager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.dao.NotificationDao;
import au.com.mason.expensemanager.domain.Notification;

@Component
public class NotificationService {
	
	@Autowired
	private NotificationDao notificationDao;
	
	public Notification create(Notification notification) {
		return notificationDao.create(notification);
	}
	
	public List<Notification> getAll() throws Exception {
		return notificationDao.getNotRemoved();
	}
	
	public Notification markRead(Long id) throws Exception {
		Notification notification = notificationDao.getById(id);
		notification.setRead(true);
		
		return notificationDao.update(notification);
	}

	public Notification markRemoved(Long id) throws Exception {
		Notification notification = notificationDao.getById(id);
		notification.setRemoved(true);

		return notificationDao.update(notification);
	}
	
	public Notification markUnRead(Long id) throws Exception {
		Notification notification = notificationDao.getById(id);
		notification.setRead(false);
		
		return notificationDao.update(notification);
	}

}
