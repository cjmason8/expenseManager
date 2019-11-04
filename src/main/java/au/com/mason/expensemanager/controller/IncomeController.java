package au.com.mason.expensemanager.controller;

import java.time.DayOfWeek;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import au.com.mason.expensemanager.config.SpringContext;
import au.com.mason.expensemanager.domain.Income;
import au.com.mason.expensemanager.dto.IncomeDto;
import au.com.mason.expensemanager.service.IncomeService;
import au.com.mason.expensemanager.util.DateUtil;
import au.com.mason.expensemanager.util.DocumentUtil;

@RestController
public class IncomeController extends BaseController<IncomeDto, Income> {
	
	@Autowired
	private IncomeService incomeService;
	
	private static Logger LOGGER = LogManager.getLogger(IncomeController.class);
	private static Gson gson = new GsonBuilder().serializeNulls().create();
	
	@RequestMapping(value = "/incomes/{id}", method = RequestMethod.GET, produces = "application/json")
	IncomeDto getIncome(@PathVariable Long id) throws Exception {
		LOGGER.info("entering IncomeController getIncome - " + id);
		Income income = incomeService.getById(id);
		LOGGER.info("leaving IncomeController getIncome - " + id);
		
		return convertToDto(income);
    }
	
	@RequestMapping(value = "/incomes", method = RequestMethod.POST, produces = "application/json", 
			consumes = "application/json", headers = "Accept=application/json")
	IncomeDto addIncome(@RequestBody IncomeDto incomeDto) throws Exception {
		LOGGER.info("entering IncomeController addIncome - " + incomeDto.getAmount());
		Income income = incomeService.addTransaction(convertToEntity(incomeDto));
		LOGGER.info("leaving IncomeController addIncome - " + incomeDto.getAmount());

		return convertToDto(income);
    }
	
	@RequestMapping(value = "/incomes/{id}", method = RequestMethod.PUT, produces = "application/json", 
			consumes = "application/json", headers = "Accept=application/json")
	IncomeDto updateIncome(@RequestBody IncomeDto incomeDto, Long id) throws Exception {
		LOGGER.info("entering IncomeController updateIncome - " + id);
		Income income = incomeService.updateTransaction(convertToEntity(incomeDto));
		LOGGER.info("leaving IncomeController updateIncome - " + id);
		
		return convertToDto(income);
    }
	
	@RequestMapping(value = "/incomes/{id}", method = RequestMethod.DELETE, produces = "application/json",
			consumes = "application/json", headers = "Accept=application/json")
	String deleteIncome(@PathVariable Long id) throws Exception {
		LOGGER.info("entering IncomeController deleteIncome - " + id);
		incomeService.deleteTransaction(id);
		LOGGER.info("leaving IncomeController deleteIncome - " + id);
		
		return "{\"status\":\"success\"}";
    }
	
	public IncomeDto convertToDto(Income income) {
		IncomeDto incomeDto = SpringContext.getApplicationContext().getBean(ModelMapper.class).map(income, IncomeDto.class);
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
    		incomeDto.setDocumentDto(DocumentUtil.convertToDto(income.getDocument()));
    	}
    	
	    return incomeDto;
	}
	
	public Income convertToEntity(IncomeDto incomeDto) {
		Income income = SpringContext.getApplicationContext().getBean(ModelMapper.class).map(incomeDto, Income.class);
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
			income.setDocument(DocumentUtil.convertToEntity(incomeDto.getDocumentDto()));
		}
    	
	    return income;
	}
}
