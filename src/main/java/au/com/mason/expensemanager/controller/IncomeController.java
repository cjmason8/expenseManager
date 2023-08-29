package au.com.mason.expensemanager.controller;

import au.com.mason.expensemanager.dto.DeleteIncomeResponseDto;
import au.com.mason.expensemanager.mapper.IncomeMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import au.com.mason.expensemanager.domain.Income;
import au.com.mason.expensemanager.dto.IncomeDto;
import au.com.mason.expensemanager.service.IncomeService;

@RestController
public class IncomeController extends BaseController<IncomeDto, Income> {
	
	@Autowired
	private IncomeService incomeService;
	
	@Autowired
	private IncomeMapper incomeMapper;
	
	private static Logger LOGGER = LogManager.getLogger(IncomeController.class);
	
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
	DeleteIncomeResponseDto deleteIncome(@PathVariable Long id) {
		LOGGER.info("entering IncomeController deleteIncome - " + id);
		incomeService.deleteTransaction(id);
		LOGGER.info("leaving IncomeController deleteIncome - " + id);
		
		return new DeleteIncomeResponseDto("success");
    }
	
	public IncomeDto convertToDto(Income income) {
		return incomeMapper.incomeToIncomeDto(income);
	}
	
	public Income convertToEntity(IncomeDto incomeDto) {
		return incomeMapper.incomeDtoToIncome(incomeDto);
	}
}
