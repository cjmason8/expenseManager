package au.com.mason.expensemanager.mapper;

import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.domain.Notification;
import au.com.mason.expensemanager.dto.NotificationDto;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    @Autowired
    private ExpenseMapper expenseMapper;

    public Notification notificationDtoToNotification(NotificationDto notificationDto) {
        if ( notificationDto == null ) {
            return null;
        }

        Notification notification = new Notification();

        if ( notificationDto.getId() != null ) {
            notification.setId( notificationDto.getId() );
        }
        notification.setMessage( notificationDto.getMessage() );
        notification.setExpense( expenseMapper.expenseDtoToExpense( notificationDto.getExpense() ) );

        return notification;
    }

    public NotificationDto notificationToNotificationDto(Notification notification) {
        if ( notification == null ) {
            return null;
        }

        NotificationDto notificationDto = new NotificationDto();

        notificationDto.setId( notification.getId() );
        notificationDto.setExpense( expenseMapper.expenseToExpenseDto( notification.getExpense() ) );
        notificationDto.setMessage( notification.getMessage() );

        return notificationDto;
    }
}
