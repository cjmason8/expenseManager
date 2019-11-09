package au.com.mason.expensemanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Income;
import au.com.mason.expensemanager.dto.IncomeDto;

@Component
@Mapper(uses = {RefDataMapper.class})
public interface IncomeMapper {
	
	IncomeMapper INSTANCE = Mappers.getMapper( IncomeMapper.class );
	 
	@Mapping(source = "transactionType", target = "entryType")
	Income incomeDtoToIncome(IncomeDto incomeDto) throws Exception;
    
	@Mapping(source = "transactionType", target = "entryType")
	Income incomeDtoToIncome(IncomeDto incomeDto, @MappingTarget Income income) throws Exception;
    
	@Mapping(source = "entryType", target = "transactionType")
	IncomeDto incomeToIncomeDto(Income income) throws Exception;

}
