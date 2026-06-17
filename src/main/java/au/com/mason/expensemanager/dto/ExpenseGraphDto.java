package au.com.mason.expensemanager.dto;

public class ExpenseGraphDto {

	private String[] labels;
	private GraphDto[] datasets;

	public ExpenseGraphDto(String[] labels, GraphDto[] datasets) {
		this.labels = labels;
		this.datasets = datasets;
	}

	public String[] getLabels() {
		return labels;
	}

	public void setLabels(String[] labels) {
		this.labels = labels;
	}

	public GraphDto[] getDatasets() {
		return datasets;
	}

	public void setDatasets(GraphDto[] datasets) {
		this.datasets = datasets;
	}

}
