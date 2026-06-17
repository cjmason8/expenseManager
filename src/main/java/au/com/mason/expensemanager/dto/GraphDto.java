package au.com.mason.expensemanager.dto;

import java.math.BigDecimal;

public class GraphDto {
	private String label;
	private BigDecimal[] data;

	public GraphDto(String label, BigDecimal[] data) {
		this.label = label;
		this.data = data;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public BigDecimal[] getData() {
		return data;
	}
	public void setData(BigDecimal[] data) {
		this.data = data;
	}

}
