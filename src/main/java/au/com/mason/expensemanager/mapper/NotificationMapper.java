package au.com.mason.expensemanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Notification;
import au.com.mason.expensemanager.dto.NotificationDto;

@Component
@Mapper(uses = {ExpenseMapper.class})
public interface NotificationMapper {
	
	NotificationMapper INSTANCE = Mappers.getMapper( NotificationMapper.class );
	 
    Notification notificationDtoToNotification(NotificationDto notificationDto) throws Exception;
    
    Notification notificationDtoToNotification(NotificationDto notificationDto, @MappingTarget Notification notification) throws Exception;
    
    NotificationDto notificationToNotificationDto(Notification notification) throws Exception;

}
