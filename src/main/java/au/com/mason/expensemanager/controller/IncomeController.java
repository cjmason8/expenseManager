package au.com.mason.expensemanager.controller;

import au.com.mason.expensemanager.domain.Income;
import au.com.mason.expensemanager.dto.StatusResponseDto;
import au.com.mason.expensemanager.dto.IncomeDto;
import au.com.mason.expensemanager.mapper.IncomeMapper;
import au.com.mason.expensemanager.service.IncomeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IncomeController extends BaseController<Income, IncomeDto> {
	
	@Autowired
	private IncomeService incomeService;

	@Autowired
	public IncomeController(IncomeMapper incomeMapper) {
		super(incomeMapper);
	}
	
	private static final Logger LOGGER = LogManager.getLogger(IncomeController.class);
	
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
	StatusResponseDto deleteIncome(@PathVariable Long id) throws Exception {
		LOGGER.info("entering IncomeController deleteIncome - " + id);
		incomeService.deleteTransaction(id);
		LOGGER.info("leaving IncomeController deleteIncome - " + id);
		
		return new StatusResponseDto("success");
    }
	
}
