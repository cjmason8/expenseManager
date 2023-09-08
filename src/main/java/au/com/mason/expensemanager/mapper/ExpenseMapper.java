package au.com.mason.expensemanager.mapper;

import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.dto.ExpenseDto;
import au.com.mason.expensemanager.util.DateUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper implements BaseMapper<Expense, ExpenseDto> {
    private static final Gson gson = new GsonBuilder().serializeNulls().create();

    @Autowired
    private RefDataMapper refDataMapper;
    @Autowired
    private DocumentMapper documentMapper;


    public Expense dtoToEntity(ExpenseDto expenseDto) {
        if ( expenseDto == null ) {
            return null;
        }

        Expense expense = new Expense();

        expense.setEntryType( refDataMapper.dtoToEntity( expenseDto.getTransactionType() ) );
        if ( expenseDto.getId() != null ) {
            expense.setId( expenseDto.getId() );
        }
        if ( expenseDto.getAmount() != null ) {
            expense.setAmount( new BigDecimal( expenseDto.getAmount() ) );
        }
        expense.setRecurringType( refDataMapper.dtoToEntity( expenseDto.getRecurringType() ) );
        expense.setNotes( expenseDto.getNotes() );
        expense.setPaid( expenseDto.getPaid() );
        if (!StringUtils.isEmpty(expenseDto.getDueDateString())) {
            expense.setDueDate(DateUtil.getFormattedDate(expenseDto.getDueDateString()));
        }
        if (!StringUtils.isEmpty(expenseDto.getStartDateString())) {
            expense.setStartDate(DateUtil.getFormattedDate(expenseDto.getStartDateString()));
        }
        if (!StringUtils.isEmpty(expenseDto.getEndDateString())) {
            expense.setEndDate(DateUtil.getFormattedDate(expenseDto.getEndDateString()));
        }
        expense.setMetaData((Map<String, Object>) gson.fromJson(expenseDto.getMetaDataChunk(), Map.class));
        if (expenseDto.getDocumentDto() != null) {
            expense.setDocument(documentMapper.dtoToEntity(expenseDto.getDocumentDto()));
        }

        return expense;
    }

    public ExpenseDto entityToDto(Expense expense) {
        if ( expense == null ) {
            return new ExpenseDto();
        }

        ExpenseDto expenseDto = new ExpenseDto();

        expenseDto.setTransactionType( refDataMapper.entityToDto( expense.getEntryType() ) );
        expenseDto.setId( expense.getId() );
        if ( expense.getAmount() != null ) {
            expenseDto.setAmount( expense.getAmount().toString() );
        }
        expenseDto.setRecurringType( refDataMapper.entityToDto( expense.getRecurringType() ) );
        expenseDto.setNotes( expense.getNotes() );
        expenseDto.setPaid( expense.getPaid() );
        if (expense.getDueDate() != null) {
            expenseDto.setDueDateString(DateUtil.getFormattedDateString(expense.getDueDate()));
            expenseDto.setWeek(DateUtil.getFormattedDateString(expense.getDueDate().with(DayOfWeek.MONDAY)));
        }

        if (expense.getStartDate() != null) {
            expenseDto.setStartDateString(DateUtil.getFormattedDateString(expense.getStartDate()));
            expenseDto.setWeek(DateUtil.getFormattedDateString(expense.getStartDate().with(DayOfWeek.MONDAY)));
        }
        if (expense.getEndDate() != null) {
            expenseDto.setEndDateString(DateUtil.getFormattedDateString(expense.getEndDate()));
        }
        expenseDto.setMetaDataChunk(gson.toJson(expense.getMetaData(), Map.class));
        if (expense.getDocument() != null) {
            expenseDto.setDocumentDto(documentMapper.entityToDto(expense.getDocument()));
        }

        return expenseDto;
    }
}
