package au.com.mason.expensemanager.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="rentalpayments")
public class RentalPayment implements Metadata {
	
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
