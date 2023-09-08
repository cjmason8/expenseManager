package au.com.mason.expensemanager.mapper;

import au.com.mason.expensemanager.domain.Income;
import au.com.mason.expensemanager.dto.IncomeDto;
import au.com.mason.expensemanager.util.DateUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IncomeMapper implements BaseMapper<Income, IncomeDto> {
    private static Gson gson = new GsonBuilder().serializeNulls().create();

    @Autowired
    private RefDataMapper refDataMapper;
    @Autowired
    private DocumentMapper documentMapper;

    public Income dtoToEntity(IncomeDto incomeDto) {
        if ( incomeDto == null ) {
            return null;
        }

        Income income = new Income();

        income.setEntryType( refDataMapper.dtoToEntity( incomeDto.getTransactionType() ) );
        if ( incomeDto.getId() != null ) {
            income.setId( incomeDto.getId() );
        }
        if ( incomeDto.getAmount() != null ) {
            income.setAmount( new BigDecimal( incomeDto.getAmount() ) );
        }
        income.setRecurringType( refDataMapper.dtoToEntity( incomeDto.getRecurringType() ) );
        income.setNotes( incomeDto.getNotes() );
        if (incomeDto.getDueDateString() != null) {
            income.setDueDate(DateUtil.getFormattedDate(incomeDto.getDueDateString()));
        }

        if (incomeDto.getStartDateString() != null) {
            income.setStartDate(DateUtil.getFormattedDate(incomeDto.getStartDateString()));
        }
        if (incomeDto.getEndDateString() != null) {
            income.setEndDate(DateUtil.getFormattedDate(incomeDto.getEndDateString()));
        }
        income.setMetaData((Map<String, Object>) gson.fromJson(incomeDto.getMetaDataChunk(), Map.class));
        if (incomeDto.getDocumentDto() != null) {
            income.setDocument(documentMapper.dtoToEntity(incomeDto.getDocumentDto()));
        }

        return income;
    }

    public IncomeDto entityToDto(Income income) {
        if ( income == null ) {
            return null;
        }

        IncomeDto incomeDto = new IncomeDto();

        incomeDto.setTransactionType( refDataMapper.entityToDto( income.getEntryType() ) );
        incomeDto.setId( income.getId() );
        if ( income.getAmount() != null ) {
            incomeDto.setAmount( income.getAmount().toString() );
        }
        incomeDto.setRecurringType( refDataMapper.entityToDto( income.getRecurringType() ) );
        incomeDto.setNotes( income.getNotes() );
        if (income.getDueDate() != null) {
            incomeDto.setDueDateString(DateUtil.getFormattedDateString(income.getDueDate()));
            incomeDto.setWeek(DateUtil.getFormattedDateString(income.getDueDate().with(DayOfWeek.MONDAY)));
        }

        if (income.getStartDate() != null) {
            incomeDto.setStartDateString(DateUtil.getFormattedDateString(income.getStartDate()));
            incomeDto.setWeek(DateUtil.getFormattedDateString(income.getStartDate().with(DayOfWeek.MONDAY)));
        }
        if (income.getEndDate() != null) {
            incomeDto.setEndDateString(DateUtil.getFormattedDateString(income.getEndDate()));
        }
        incomeDto.setMetaDataChunk(gson.toJson(income.getMetaData(), Map.class));
        if (income.getDocument() != null) {
            incomeDto.setDocumentDto(documentMapper.entityToDto(income.getDocument()));
        }

        return incomeDto;
    }
}
