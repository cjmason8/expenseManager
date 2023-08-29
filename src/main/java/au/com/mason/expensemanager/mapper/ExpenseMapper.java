package au.com.mason.expensemanager.mapper;

import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.dto.ExpenseDto;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    @Autowired
    private RefDataMapper refDataMapper;

    public Expense expenseDtoToExpense(ExpenseDto expenseDto) {
        if ( expenseDto == null ) {
            return null;
        }

        Expense expense = new Expense();

        expense.setEntryType( refDataMapper.refDataDtoToRefData( expenseDto.getTransactionType() ) );
        if ( expenseDto.getId() != null ) {
            expense.setId( expenseDto.getId() );
        }
        if ( expenseDto.getAmount() != null ) {
            expense.setAmount( new BigDecimal( expenseDto.getAmount() ) );
        }
        expense.setRecurringType( refDataMapper.refDataDtoToRefData( expenseDto.getRecurringType() ) );
        expense.setNotes( expenseDto.getNotes() );
        expense.setPaid( expenseDto.getPaid() );

        return expense;
    }

    public Expense expenseDtoToExpense(ExpenseDto expenseDto, Expense expense) {
        if ( expenseDto == null ) {
            return null;
        }

        if ( expenseDto.getTransactionType() != null ) {
            if ( expense.getEntryType() == null ) {
                expense.setEntryType( new RefData() );
            }
            refDataMapper.refDataDtoToRefData( expenseDto.getTransactionType(), expense.getEntryType() );
        }
        else {
            expense.setEntryType( null );
        }
        if ( expenseDto.getId() != null ) {
            expense.setId( expenseDto.getId() );
        }
        if ( expenseDto.getAmount() != null ) {
            expense.setAmount( new BigDecimal( expenseDto.getAmount() ) );
        }
        else {
            expense.setAmount( null );
        }
        if ( expenseDto.getRecurringType() != null ) {
            if ( expense.getRecurringType() == null ) {
                expense.setRecurringType( new RefData() );
            }
            refDataMapper.refDataDtoToRefData( expenseDto.getRecurringType(), expense.getRecurringType() );
        }
        else {
            expense.setRecurringType( null );
        }
        expense.setNotes( expenseDto.getNotes() );
        expense.setPaid( expenseDto.getPaid() );

        return expense;
    }

    public ExpenseDto expenseToExpenseDto(Expense expense) {
        if ( expense == null ) {
            return null;
        }

        ExpenseDto expenseDto = new ExpenseDto();

        expenseDto.setTransactionType( refDataMapper.refDataToRefDataDto( expense.getEntryType() ) );
        expenseDto.setId( expense.getId() );
        if ( expense.getAmount() != null ) {
            expenseDto.setAmount( expense.getAmount().toString() );
        }
        expenseDto.setRecurringType( refDataMapper.refDataToRefDataDto( expense.getRecurringType() ) );
        expenseDto.setNotes( expense.getNotes() );
        expenseDto.setPaid( expense.getPaid() );

        return expenseDto;
    }
}
