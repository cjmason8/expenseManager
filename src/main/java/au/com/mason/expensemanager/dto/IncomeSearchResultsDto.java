package au.com.mason.expensemanager.dto;

import java.util.List;

public class IncomeSearchResultsDto {

	private List<IncomeDto> incomes;
	private List<DocumentDto> documents;
	private ExpenseGraphDto incomeGraphDto;

	public IncomeSearchResultsDto(List<IncomeDto> incomes, List<DocumentDto> documents,
		ExpenseGraphDto incomeGraphDto) {
		this.incomes = incomes;
		this.documents = documents;
		this.incomeGraphDto = incomeGraphDto;
	}

	public List<IncomeDto> getIncomes() {
		return incomes;
	}

	public List<DocumentDto> getDocuments() {
		return documents;
	}

	public ExpenseGraphDto getIncomeGraphDto() {
		return incomeGraphDto;
	}

}
