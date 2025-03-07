package au.com.mason.expensemanager.mapper;

import au.com.mason.expensemanager.domain.Notification;
import au.com.mason.expensemanager.dto.NotificationDto;
import au.com.mason.expensemanager.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper implements BaseMapper<Notification, NotificationDto> {
    @Autowired
    private ExpenseMapper expenseMapper;

    public Notification dtoToEntity(NotificationDto notificationDto) {
        if ( notificationDto == null ) {
            return null;
        }

        Notification notification = new Notification();

        if ( notificationDto.getId() != null ) {
            notification.setId( notificationDto.getId() );
        }
        notification.setMessage( notificationDto.getMessage() );
        notification.setCreated(DateUtil.getFormattedDate(notificationDto.getCreatedDateString()));
        notification.setExpense( expenseMapper.dtoToEntity( notificationDto.getExpense() ) );

        return notification;
    }

    public NotificationDto entityToDto(Notification notification) {
        if ( notification == null ) {
            return null;
        }

        NotificationDto notificationDto = new NotificationDto();

        notificationDto.setId( notification.getId() );
        notificationDto.setExpense( expenseMapper.entityToDto( notification.getExpense() ) );
        notificationDto.setMessage( notification.getMessage() );
        notificationDto.setCreatedDateString(DateUtil.getFormattedDateString(notification.getCreated()));
        notificationDto.setRead( notification.isRead() );
        notificationDto.setRemoved( notification.isRemoved() );

        return notificationDto;
    }
}
