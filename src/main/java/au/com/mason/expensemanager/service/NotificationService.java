package au.com.mason.expensemanager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.repository.NotificationRepository;
import au.com.mason.expensemanager.domain.Notification;

@Component
public class NotificationService {
	
	@Autowired
	private NotificationRepository notificationRepository;
	
	public Notification create(Notification notification) {
		return notificationRepository.create(notification);
	}
	
	public List<Notification> getAll() throws Exception {
		return notificationRepository.getUnread();
	}
	
	public Notification markRead(Long id) {
		Notification notification = notificationRepository.getById(id);
		notification.setRead(true);
		
		return notificationRepository.update(notification);
	}
	
	public Notification markUnRead(Long id) {
		Notification notification = notificationRepository.getById(id);
		notification.setRead(false);
		
		return notificationRepository.update(notification);
	}

}
