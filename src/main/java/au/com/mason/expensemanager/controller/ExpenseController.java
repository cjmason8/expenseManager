package au.com.mason.expensemanager.controller;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import au.com.mason.expensemanager.config.SpringContext;
import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.dto.ExpenseDto;
import au.com.mason.expensemanager.service.ExpenseService;
import au.com.mason.expensemanager.util.DateUtil;
import au.com.mason.expensemanager.util.DocumentUtil;

@RestController
public class ExpenseController extends BaseController<ExpenseDto, Expense> {
	
	private static Logger LOGGER = LogManager.getLogger(ExpenseController.class);
	private static Gson gson = new GsonBuilder().serializeNulls().create();
	
	@Autowired
	private ExpenseService expenseService;
	
	@RequestMapping(value = "/expenses", method = RequestMethod.GET, produces = "application/json")
	List<ExpenseDto> getExpenses() throws Exception {
		LOGGER.info("entering ExpenseController getExpenses");		
		List<Expense> results = expenseService.getAll();
		
		LOGGER.info("leaving ExpenseController getExpenses");
		
		return convertList(results);
    }
	
	@RequestMapping(value = "/expenses/{id}", method = RequestMethod.GET, produces = "application/json")
	ExpenseDto getExpense(@PathVariable Long id) throws Exception {
		LOGGER.info("entering ExpenseController getExpense - " + id);		
		Expense result = expenseService.getById(id);
		
		LOGGER.info("leaving ExpenseController getExpense - " + id);
		
		return convertToDto(result);
    }
	
	@RequestMapping(value = "/expenses/pay/{id}", method = RequestMethod.GET, produces = "application/json")
	ExpenseDto payExpense(@PathVariable Long id) throws Exception {
		LOGGER.info("entering ExpenseController payExpense - " + id);		
		Expense result = expenseService.payExpense(id);
		
		LOGGER.info("leaving ExpenseController payExpense - " + id);
		
		return convertToDto(result);
    }
	
	@RequestMapping(value = "/expenses/unpay/{id}", method = RequestMethod.GET, produces = "application/json")
	ExpenseDto unPayExpense(@PathVariable Long id) throws Exception {
		LOGGER.info("entering ExpenseController unPayExpense - " + id);
		Expense result = expenseService.unPayExpense(id);
		
		LOGGER.info("leaving ExpenseController unPayExpense - " + id);
		
		return convertToDto(result);
    }	
	
	@RequestMapping(value = "/expenses", method = RequestMethod.POST, produces = "application/json", 
			consumes = "application/json", headers = "Accept=application/json")
	ExpenseDto addExpense(@RequestBody ExpenseDto expense) throws Exception {
		LOGGER.info("entering ExpenseController addExpense - " + expense.getTransactionType() + ", " + expense.getAmount());		
		Expense result = expenseService.addTransaction(convertToEntity(expense));
		
		LOGGER.info("leaving ExpenseController addExpense - " + expense.getTransactionType() + ", " + expense.getAmount());
		
		return convertToDto(result);
    }
	
	@RequestMapping(value = "/expenses/{id}", method = RequestMethod.PUT, produces = "application/json", 
			consumes = "application/json", headers = "Accept=application/json")
	ExpenseDto updateExpense(@RequestBody ExpenseDto expense, Long id) throws Exception {
		LOGGER.info("entering ExpenseController updateExpense - " + id);		
		Expense result = expenseService.updateTransaction(convertToEntity(expense));
		
		LOGGER.info("leaving ExpenseController updateExpense - " + id);

		return convertToDto(result);
    }
	
	@RequestMapping(value = "/expenses/{id}", method = RequestMethod.DELETE, produces = "application/json",
			consumes = "application/json", headers = "Accept=application/json")
	String deleteExpense(@PathVariable Long id) throws Exception {
		LOGGER.info("entering ExpenseController deleteExpense - " + id);		
		expenseService.deleteTransaction(id);
		
		LOGGER.info("leaving ExpenseController deleteExpense - " + id);
		
		return "{\"status\":\"success\"}";
    }
	
	public ExpenseDto convertToDto(Expense expense) {
		ExpenseDto expenseDto = SpringContext.getApplicationContext().getBean(ModelMapper.class).map(expense, ExpenseDto.class);
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
    		expenseDto.setDocumentDto(DocumentUtil.convertToDto(expense.getDocument()));
    	}
    	
    	return expenseDto;
	}
	
	public Expense convertToEntity(ExpenseDto expenseDto) {
		Expense expense = SpringContext.getApplicationContext().getBean(ModelMapper.class).map(expenseDto, Expense.class);
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
			expense.setDocument(DocumentUtil.convertToEntity(expenseDto.getDocumentDto()));
		}
    	
	    return expense;
	}
}
