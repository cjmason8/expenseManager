package au.com.mason.expensemanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.dao.RentalPaymentDao;
import au.com.mason.expensemanager.domain.RentalPayment;

@Component
public class RentalPaymentService {
	
	@Autowired
	private RentalPaymentDao rentalPaymentDao;
	
	@Autowired
	protected DocumentService documentService;
	
	public RentalPayment createDonation(RentalPayment rentalPayment) throws Exception {
		
		rentalPaymentDao.create(rentalPayment);
		
		return rentalPayment;
	}
	
}
