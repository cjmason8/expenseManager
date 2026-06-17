package au.com.mason.expensemanager.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import au.com.mason.expensemanager.domain.converter.TimestampLocalDateConverter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rentalpayments")
@Getter
@Setter
@NoArgsConstructor
public class RentalPayment {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "rentalpayments_seq")
	@SequenceGenerator(name = "rentalpayments_seq", sequenceName = "rentalpayments_seq", allocationSize = 1)
	private long id;

	private BigDecimal managementFee = new BigDecimal(0);
	private BigDecimal adminFee = new BigDecimal(0);
	private BigDecimal otherFee = new BigDecimal(0);
	private BigDecimal totalRent = new BigDecimal(0);

	@Convert(converter = TimestampLocalDateConverter.class)
	private LocalDate statementFrom;

	@Convert(converter = TimestampLocalDateConverter.class)
	private LocalDate statementTo;

	private String property;

	@OneToOne
	@JoinColumn(name = "documentId")
	private Document document;

	public BigDecimal getPaymentToOwner() {
		return totalRent.subtract(adminFee).subtract(managementFee);
	}

}
