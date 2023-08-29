package au.com.mason.expensemanager.mapper;

import au.com.mason.expensemanager.domain.Income;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.dto.IncomeDto;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IncomeMapper {

    @Autowired
    private RefDataMapper refDataMapper;

    public Income incomeDtoToIncome(IncomeDto incomeDto) {
        if ( incomeDto == null ) {
            return null;
        }

        Income income = new Income();

        income.setEntryType( refDataMapper.refDataDtoToRefData( incomeDto.getTransactionType() ) );
        if ( incomeDto.getId() != null ) {
            income.setId( incomeDto.getId() );
        }
        if ( incomeDto.getAmount() != null ) {
            income.setAmount( new BigDecimal( incomeDto.getAmount() ) );
        }
        income.setRecurringType( refDataMapper.refDataDtoToRefData( incomeDto.getRecurringType() ) );
        income.setNotes( incomeDto.getNotes() );

        return income;
    }

    public Income incomeDtoToIncome(IncomeDto incomeDto, Income income) {
        if ( incomeDto == null ) {
            return null;
        }

        if ( incomeDto.getTransactionType() != null ) {
            if ( income.getEntryType() == null ) {
                income.setEntryType( new RefData() );
            }
            refDataMapper.refDataDtoToRefData( incomeDto.getTransactionType(), income.getEntryType() );
        }
        else {
            income.setEntryType( null );
        }
        if ( incomeDto.getId() != null ) {
            income.setId( incomeDto.getId() );
        }
        if ( incomeDto.getAmount() != null ) {
            income.setAmount( new BigDecimal( incomeDto.getAmount() ) );
        }
        else {
            income.setAmount( null );
        }
        if ( incomeDto.getRecurringType() != null ) {
            if ( income.getRecurringType() == null ) {
                income.setRecurringType( new RefData() );
            }
            refDataMapper.refDataDtoToRefData( incomeDto.getRecurringType(), income.getRecurringType() );
        }
        else {
            income.setRecurringType( null );
        }
        income.setNotes( incomeDto.getNotes() );

        return income;
    }

    public IncomeDto incomeToIncomeDto(Income income) {
        if ( income == null ) {
            return null;
        }

        IncomeDto incomeDto = new IncomeDto();

        incomeDto.setTransactionType( refDataMapper.refDataToRefDataDto( income.getEntryType() ) );
        incomeDto.setId( income.getId() );
        if ( income.getAmount() != null ) {
            incomeDto.setAmount( income.getAmount().toString() );
        }
        incomeDto.setRecurringType( refDataMapper.refDataToRefDataDto( income.getRecurringType() ) );
        incomeDto.setNotes( income.getNotes() );

        return incomeDto;
    }
}
