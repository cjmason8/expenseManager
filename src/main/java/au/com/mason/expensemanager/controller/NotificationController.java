package au.com.mason.expensemanager.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import au.com.mason.expensemanager.dto.NotificationDto;
import au.com.mason.expensemanager.service.NotificationService;

@RestController
@CrossOrigin
public class NotificationController {
	
	@Autowired
	private NotificationService notificationService;
	
	private static Logger LOGGER = LogManager.getLogger(NotificationController.class);
	
	@RequestMapping(value = "/notifications", method = RequestMethod.GET, produces = "application/json")
	List<NotificationDto> getNotifications() throws Exception {
		LOGGER.info("entering NotificationController getNotification");
		List<NotificationDto> results = notificationService.getAll();
		LOGGER.info("leaving NotificationController getNotifications");
		return results;
    }
	
	@RequestMapping(value = "/notifications/markRead/{id}", method = RequestMethod.GET, produces = "application/json")
	NotificationDto markRead(@PathVariable Long id) throws Exception {
		LOGGER.info("entering NotificationController markRead - " + id);		
		NotificationDto result = notificationService.markRead(id);
		
		LOGGER.info("leaving NotificationController markRead - " + id);
		
		return result;
    }
	
	@RequestMapping(value = "/notifications/markUnRead/{id}", method = RequestMethod.GET, produces = "application/json")
	NotificationDto markUnRead(@PathVariable Long id) throws Exception {
		LOGGER.info("entering NotificationController markUnRead - " + id);		
		NotificationDto result = notificationService.markUnRead(id);
		
		LOGGER.info("leaving NotificationController markUnRead - " + id);
		
		return result;
    }
	
}
