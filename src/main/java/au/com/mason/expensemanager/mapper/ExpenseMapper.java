package au.com.mason.expensemanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.dto.ExpenseDto;

@Component
@Mapper(uses = {RefDataMapper.class})
public interface ExpenseMapper {
	
	ExpenseMapper INSTANCE = Mappers.getMapper( ExpenseMapper.class );
	 
	@Mapping(source = "transactionType", target = "entryType")
	Expense expenseDtoToExpense(ExpenseDto expenseDto) throws Exception;
    
	@Mapping(source = "transactionType", target = "entryType")
	Expense expenseDtoToExpense(ExpenseDto expenseDto, 
    		@MappingTarget Expense expense) throws Exception;
    
	@Mapping(source = "entryType", target = "transactionType")
	ExpenseDto expenseToExpenseDto(Expense expense) throws Exception;

}
