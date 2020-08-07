package au.com.mason.expensemanager.dto;

import java.util.List;

public class RentalPaymentInfoDto {
	
	private List<RentalPaymentDto> rentalPayments;
	private Integer previousYear;
	private Integer nextYear;
	
	public RentalPaymentInfoDto(List<RentalPaymentDto> rentalPayments, Integer previousYear, Integer nextYear) {
		super();
		this.rentalPayments = rentalPayments;
		this.previousYear = previousYear;
		this.nextYear = nextYear;
	}
	
	public List<RentalPaymentDto> getRentalPayments() {
		return rentalPayments;
	}
	
	public void setRentalPayments(List<RentalPaymentDto> rentalPayments) {
		this.rentalPayments = rentalPayments;
	}

	public Integer getPreviousYear() {
		return previousYear;
	}

	public void setPreviousYear(Integer previousYear) {
		this.previousYear = previousYear;
	}

	public Integer getNextYear() {
		return nextYear;
	}

	public void setNextYear(Integer nextYear) {
		this.nextYear = nextYear;
	}

}
