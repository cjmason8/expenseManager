package au.com.mason.expensemanager.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import au.com.mason.expensemanager.dto.RentalPaymentDto;
import au.com.mason.expensemanager.service.RentalPaymentService;

@RestController
public class RentalPaymentController {
	
	@Autowired
	private RentalPaymentService rentalPaymentService;
	
	private static Logger LOGGER = LogManager.getLogger(RentalPaymentController.class);
	
	@RequestMapping(value = "/rentalPayments/getByProperty/{property}", method = RequestMethod.GET, produces = "application/json")
	List<RentalPaymentDto> getRentalPayments(@PathVariable String property) throws Exception {
		LOGGER.info("entering RentalPaymentController getRentalPayment");
		List<RentalPaymentDto> results = rentalPaymentService.getAll(property);
		LOGGER.info("leaving RentalPaymentController getRentalPayments");

		return results;
    }
	
	@PostMapping(value = "/rentalPayments", produces = "application/json", consumes = "application/json")
	RentalPaymentDto addRentalPayment(@RequestBody RentalPaymentDto rentalPaymentDto) throws Exception {
		LOGGER.info("entering RentalPaymentController addRentalPayment - " + rentalPaymentDto.getStatementFromString() + ", " + rentalPaymentDto.getStatementToString() + ", " + rentalPaymentDto.getProperty());
		RentalPaymentDto rentalPayment = rentalPaymentService.createRentalPayment(rentalPaymentDto);
		LOGGER.info("leaving RentalPaymentController addRentalPayment - " + rentalPaymentDto.getStatementFromString() + ", " + rentalPaymentDto.getStatementToString() + ", " + rentalPaymentDto.getProperty());
		
		return rentalPayment;
    }
	
	@RequestMapping(value = "/rentalPayments/{id}", method = RequestMethod.PUT, produces = "application/json", 
			consumes = "application/json", headers = "Accept=application/json")
	RentalPaymentDto updateDonation(@RequestBody RentalPaymentDto rentalPaymentDto, Long id) throws Exception {
		LOGGER.info("entering RentalPaymentController updateRentalPayment - " + id);
		RentalPaymentDto rentalPayment = rentalPaymentService.updateRentalPayment(rentalPaymentDto);
		LOGGER.info("leaving RentalPaymentController updateRentalPayment - " + id);
		
		return rentalPayment;
    }
	
	
	@RequestMapping(value = "/rentalPayments/{id}", method = RequestMethod.DELETE, produces = "application/json",
			consumes = "application/json", headers = "Accept=application/json")
	String deleteRentalPayment(@PathVariable Long id) throws Exception {
		LOGGER.info("entering RentalPaymentController deleteRentalPayment - " + id);
		rentalPaymentService.deleteRentalPayment(id);
		LOGGER.info("leaving RentalPaymentController deleteRentalPayment - " + id);
		
		return "{\"status\":\"success\"}";
    }
	
	@RequestMapping(value = "/rentalPayments/{id}", method = RequestMethod.GET, produces = "application/json")
	RentalPaymentDto getRentalPayment(@PathVariable Long id) throws Exception {
		LOGGER.info("entering RentalPaymentController getRentalPayment - " + id);
		RentalPaymentDto rentalPayment = rentalPaymentService.getRentalPayment(id);
		LOGGER.info("leaving RentalPaymentController getRentalPayment - " + id);
		
		return rentalPayment;
    }
	
}
