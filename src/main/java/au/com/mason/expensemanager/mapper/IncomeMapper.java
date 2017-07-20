package au.com.mason.expensemanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Income;
import au.com.mason.expensemanager.dto.IncomeDto;

@Component
@Mapper
public interface IncomeMapper {
	
	IncomeMapper INSTANCE = Mappers.getMapper( IncomeMapper.class );
	 
	Income incomeDtoToIncome(IncomeDto incomeDto) throws Exception;
    
    Income incomeDtoToIncome(IncomeDto incomeDto, @MappingTarget Income income) throws Exception;
    
	@Mappings({
	      @Mapping(target="recurringTypeId", source="income.recurringType.id")
	    })
    IncomeDto incomeToIncomeDto(Income income) throws Exception;

}