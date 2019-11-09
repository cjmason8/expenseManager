package au.com.mason.expensemanager.mapper;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Notification;
import au.com.mason.expensemanager.dto.ExpenseDto;
import au.com.mason.expensemanager.dto.NotificationDto;
import au.com.mason.expensemanager.util.DateUtil;

@Component
public class NotificationMapperWrapper {
	
	private NotificationMapper notificationMapper = NotificationMapper.INSTANCE;
	
	public Notification notificationDtoToNotification(NotificationDto notificationDto) throws Exception {
		Notification notification = notificationMapper.notificationDtoToNotification(notificationDto);
		notification.setCreated(DateUtil.getFormattedDate(notificationDto.getCreatedDateString()));
		
		return notification;
	}

    public Notification notificationDtoToNotification(NotificationDto notificationDto, Notification notificationParam) throws Exception {
    	Notification notification = notificationMapper.notificationDtoToNotification(notificationDto, notificationParam);
    	notification.setCreated(DateUtil.getFormattedDate(notificationDto.getCreatedDateString()));
    	
		return notification;
    }
    
    public NotificationDto notificationToNotificationDto(Notification notification) throws Exception {
    	NotificationDto notificationDto = notificationMapper.notificationToNotificationDto(notification);
    	notificationDto.setCreatedDateString(DateUtil.getFormattedDateString(notification.getCreated()));
    	if (notification.getExpense() != null) {
    		notificationDto.getExpense().setDueDateString(DateUtil.getFormattedDateString(notification.getExpense().getDueDate()));
    	}
    	else {
    		notificationDto.setExpense(new ExpenseDto());
    	}

		return notificationDto;
    }

}
