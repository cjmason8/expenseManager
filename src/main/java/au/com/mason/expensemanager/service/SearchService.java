package au.com.mason.expensemanager.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.controller.ExpenseController;
import au.com.mason.expensemanager.dao.DocumentDao;
import au.com.mason.expensemanager.dao.TransactionDao;
import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.dto.DocumentDto;
import au.com.mason.expensemanager.dto.ExpenseGraphDto;
import au.com.mason.expensemanager.dto.GraphDto;
import au.com.mason.expensemanager.dto.SearchParamsDto;
import au.com.mason.expensemanager.dto.SearchResultsDto;
import au.com.mason.expensemanager.util.DocumentUtil;

@Component
public class SearchService {
	
	@Autowired
	private TransactionDao<Expense> expenseDao;
	
	@Autowired
	private DocumentDao documentDao;
	
	@Autowired
	private ExpenseController expenseController;
	
	public SearchResultsDto findSearchResults(SearchParamsDto searchParamsDto) throws Exception {
		List<Expense> expenses = expenseDao.findExpenses(searchParamsDto);
		List<Document> documents = documentDao.findDocuments(searchParamsDto);
		List<DocumentDto> documentDtos = new ArrayList<>();
		for (Document document : documents) {
			documentDtos.add(DocumentUtil.convertToDto(document));
		}
		
		int paidExpensesSize = expenses.stream().filter(expense -> expense.getPaid()).collect(Collectors.toList()).size();
		String[] labels = new String[paidExpensesSize];
		BigDecimal[] data = new BigDecimal[paidExpensesSize];
		int count = 0;
		
		for (Expense expense : expenses) {
			if (expense.getPaid()) {
				labels[count] = expense.getDueDate().getMonthValue() + "/" + expense.getDueDate().getYear();
				data[count++] = expense.getAmount();
			}
		}
		
		String description = null;
		if (searchParamsDto.getTransactionType() == null) {
			description = searchParamsDto.getMetaDataChunk();
		}
		else {
			description = searchParamsDto.getTransactionType().getDescription();
		}
		GraphDto graphDto = new GraphDto(description, data);
		
		return new SearchResultsDto(expenseController.convertList(expenses), documentDtos, 
				new ExpenseGraphDto((String[]) labels, new GraphDto[] {graphDto}));
	}
	
}
