package au.com.mason.expensemanager.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="rentalpayments")
public class RentalPayment {
	
	public RentalPayment() {}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private BigDecimal managementFee = new BigDecimal(0);
	private BigDecimal adminFee = new BigDecimal(0);
	private BigDecimal otherFee = new BigDecimal(0);
	private BigDecimal totalRent = new BigDecimal(0);
	private LocalDate statementFrom;
	private LocalDate statementTo;
	private String property;

	@OneToOne
	@JoinColumn(name = "documentId")
	private Document document; 
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public BigDecimal getManagementFee() {
		return managementFee;
	}

	public void setManagementFee(BigDecimal managementFee) {
		this.managementFee = managementFee;
	}

	public BigDecimal getAdminFee() {
		return adminFee;
	}

	public void setAdminFee(BigDecimal adminFee) {
		this.adminFee = adminFee;
	}

	public BigDecimal getOtherFee() {
		return otherFee;
	}

	public void setOtherFee(BigDecimal otherFee) {
		this.otherFee = otherFee;
	}

	public BigDecimal getTotalRent() {
		return totalRent;
	}

	public void setTotalRent(BigDecimal totalRent) {
		this.totalRent = totalRent;
	}

	public LocalDate getStatementFrom() {
		return statementFrom;
	}

	public void setStatementFrom(LocalDate statementFrom) {
		this.statementFrom = statementFrom;
	}

	public LocalDate getStatementTo() {
		return statementTo;
	}

	public void setStatementTo(LocalDate statementTo) {
		this.statementTo = statementTo;
	}
	
	public BigDecimal getPaymentToOwner() {
		return totalRent.subtract(adminFee).subtract(managementFee);
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

}
