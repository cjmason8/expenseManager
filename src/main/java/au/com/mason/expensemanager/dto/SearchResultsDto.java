package au.com.mason.expensemanager.dto;

import java.util.List;

public class SearchResultsDto {

	private List<ExpenseDto> expenses;
	private List<DocumentDto> documents;
	private ExpenseGraphDto expenseGraphDto;

	public SearchResultsDto(List<ExpenseDto> expenses, List<DocumentDto> documents, ExpenseGraphDto expenseGraphDto) {
		this.expenses = expenses;
		this.documents = documents;
		this.expenseGraphDto = expenseGraphDto;
	}

	public List<ExpenseDto> getExpenses() {
		return expenses;
	}

	public List<DocumentDto> getDocuments() {
		return documents;
	}

	public ExpenseGraphDto getExpenseGraphDto() {
		return expenseGraphDto;
	}

}
