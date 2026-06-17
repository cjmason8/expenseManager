package au.com.mason.expensemanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import au.com.mason.expensemanager.domain.Notification;
import au.com.mason.expensemanager.dto.NotificationDto;

@Mapper(componentModel = "spring", uses = {ExpenseMapper.class, MappingConverters.class})
public interface NotificationMapper extends BaseMapper<Notification, NotificationDto> {

	@Override
	@Mapping(source = "created", target = "createdDateString", qualifiedByName = "localDateToString")
	NotificationDto entityToDto(Notification notification);

	@Override
	@Mapping(source = "createdDateString", target = "created", qualifiedByName = "stringToLocalDate")
	Notification dtoToEntity(NotificationDto notificationDto);

}
