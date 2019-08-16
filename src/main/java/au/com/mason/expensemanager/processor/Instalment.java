package au.com.mason.expensemanager.processor;

import java.math.BigDecimal;
import java.time.LocalDate;

import au.com.mason.expensemanager.domain.Document;

public class Instalment {
	private LocalDate dueDate;
	private String amount;
	private String notes;
	private Document document;
	
	public Instalment(LocalDate dueDate) {
		this.dueDate = dueDate;
	}
	
	public LocalDate getDueDate() {
		return dueDate;
	}
	
	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}
	
	public BigDecimal getAmount() {
		return new BigDecimal(amount);
	}
	
	public void setAmount(String amount) {
		this.amount = amount;
	}
	
	public String getNotes() {
		return notes;
	}
	
	public void setNotes(int counter) {
		switch (counter) {
		case 1:
			this.notes = "1st Instalment";
			break;
		case 2:
			this.notes = "2nd Instalment";
			break;
		case 3:
			this.notes = "3rd Instalment";
			break;
		case 4:
			this.notes = "4th Instalment";
			break;
		default:
			this.notes = "";
		}
	}
	
	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
	
}
