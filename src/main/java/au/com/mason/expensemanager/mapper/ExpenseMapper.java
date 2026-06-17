package au.com.mason.expensemanager.mapper;

import java.time.DayOfWeek;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.dto.ExpenseDto;
import au.com.mason.expensemanager.util.DateUtil;

@Mapper(componentModel = "spring", uses = {RefDataMapper.class, DocumentMapper.class, MappingConverters.class})
public interface ExpenseMapper extends BaseMapper<Expense, ExpenseDto> {

	@Override
	@Mapping(source = "entryType", target = "transactionType")
	@Mapping(source = "amount", target = "amount", qualifiedByName = "bigDecimalToString")
	@Mapping(source = "dueDate", target = "dueDateString", qualifiedByName = "localDateToString")
	@Mapping(source = "startDate", target = "startDateString", qualifiedByName = "localDateToString")
	@Mapping(source = "endDate", target = "endDateString", qualifiedByName = "localDateToString")
	@Mapping(source = "metaData", target = "metaDataChunk", qualifiedByName = "objectMapToJson")
	@Mapping(source = "document", target = "documentDto")
	@Mapping(target = "week", ignore = true)
	ExpenseDto entityToDto(Expense expense);

	@Override
	@Mapping(source = "transactionType", target = "entryType")
	@Mapping(source = "amount", target = "amount", qualifiedByName = "stringToBigDecimal")
	@Mapping(source = "dueDateString", target = "dueDate", qualifiedByName = "stringToLocalDate")
	@Mapping(source = "startDateString", target = "startDate", qualifiedByName = "stringToLocalDate")
	@Mapping(source = "endDateString", target = "endDate", qualifiedByName = "stringToLocalDate")
	@Mapping(source = "metaDataChunk", target = "metaData", qualifiedByName = "jsonToObjectMap")
	@Mapping(source = "documentDto", target = "document", conditionQualifiedByName = "hasDocumentFileName")
	@Mapping(target = "deleted", ignore = true)
	@Mapping(target = "documentationFilePath", ignore = true)
	@Mapping(target = "recurringTransaction", ignore = true)
	Expense dtoToEntity(ExpenseDto expenseDto);

	@AfterMapping
	default void setWeekFromDates(@MappingTarget ExpenseDto dto, Expense expense) {
		if (expense.getDueDate() != null) {
			dto.setWeek(DateUtil.getFormattedDateString(expense.getDueDate().with(DayOfWeek.MONDAY)));
		}
		if (expense.getStartDate() != null) {
			dto.setWeek(DateUtil.getFormattedDateString(expense.getStartDate().with(DayOfWeek.MONDAY)));
		}
	}

}
