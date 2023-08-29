package au.com.mason.expensemanager.service;

import au.com.mason.expensemanager.controller.ExpenseController;
import au.com.mason.expensemanager.repository.TransactionRepository;
import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.dto.DocumentDto;
import au.com.mason.expensemanager.dto.ExpenseGraphDto;
import au.com.mason.expensemanager.dto.GraphDto;
import au.com.mason.expensemanager.dto.SearchParamsDto;
import au.com.mason.expensemanager.dto.SearchResultsDto;
import au.com.mason.expensemanager.repository.DocumentRepository;
import au.com.mason.expensemanager.util.DocumentUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SearchService {
	
	@Autowired
	private TransactionRepository<Expense> expenseDao;
	
	@Autowired
	private DocumentRepository documentRepository;
	
	@Autowired
	private ExpenseController expenseController;
	
	public SearchResultsDto findSearchResults(SearchParamsDto searchParamsDto) throws Exception {
		List<Expense> expenses = expenseDao.findExpenses(searchParamsDto);
		List<Document> documents = documentRepository.findDocuments(searchParamsDto);
		List<DocumentDto> documentDtos = new ArrayList<>();
		for (Document document : documents) {
			documentDtos.add(DocumentUtil.convertToDto(document));
		}
		
		int paidExpensesSize = expenses.stream().filter(Expense::getPaid).toList().size();
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
				new ExpenseGraphDto(labels, new GraphDto[] {graphDto}));
	}
	
}
