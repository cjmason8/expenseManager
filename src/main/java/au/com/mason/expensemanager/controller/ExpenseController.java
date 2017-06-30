package au.com.mason.expensemanager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import au.com.mason.expensemanager.dto.ExpenseDto;
import au.com.mason.expensemanager.service.ExpenseService;

@RestController
public class ExpenseController {
	
	@Autowired
	private ExpenseService expenseService;
	
	@RequestMapping(value = "/expenses", method = RequestMethod.GET, produces = "application/json")
	List<ExpenseDto> expenses() throws Exception {
		return expenseService.getAll();
        
    }
	
	@RequestMapping(value = "/expenses/{id}", method = RequestMethod.GET, produces = "application/json")
	ExpenseDto getExpense(@PathVariable Long id) throws Exception {
		return expenseService.getById(id);
        
    }
	
	@RequestMapping(value = "/expenses", method = RequestMethod.POST, produces = "application/json", consumes = "application/json", headers = "Accept=application/json")
	ExpenseDto addExpense(@RequestBody ExpenseDto expense) throws Exception {
		
		return expenseService.addExpense(expense);
    }
	
	@RequestMapping(value = "/expenses/{id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json", headers = "Accept=application/json")
	ExpenseDto updateExpense(@RequestBody ExpenseDto expense, Long id) throws Exception {
		return expenseService.updateExpense(expense);
    }
	
	@RequestMapping(value = "/expenses/{id}", method = RequestMethod.DELETE, produces = "application/json", consumes = "application/json", headers = "Accept=application/json")
	String updateExpense(@PathVariable Long id) throws Exception {
		
		expenseService.deleteExpense(id);
		
		return "{\"status\":\"success\"}";
    }	
}