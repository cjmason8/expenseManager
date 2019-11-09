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

import au.com.mason.expensemanager.domain.RentalPayment;
import au.com.mason.expensemanager.dto.RentalPaymentDto;
import au.com.mason.expensemanager.mapper.RentalPaymentMapperWrapper;
import au.com.mason.expensemanager.service.RentalPaymentService;

@RestController
public class RentalPaymentController extends BaseController<RentalPaymentDto, RentalPayment> {
	
	@Autowired
	private RentalPaymentService rentalPaymentService;
	
	@Autowired
	private RentalPaymentMapperWrapper rentalPaymentMapperWrapper;
	
	private static Logger LOGGER = LogManager.getLogger(RentalPaymentController.class);
	
	@RequestMapping(value = "/rentalPayments/getByProperty/{property}", method = RequestMethod.GET, produces = "application/json")
	List<RentalPaymentDto> getRentalPayments(@PathVariable String property) throws Exception {
		LOGGER.info("entering RentalPaymentController getRentalPayment");
		List<RentalPayment> results = rentalPaymentService.getAll(property);
		LOGGER.info("leaving RentalPaymentController getRentalPayments");

		return convertList(results);
    }
	
	@PostMapping(value = "/rentalPayments", produces = "application/json", consumes = "application/json")
	RentalPaymentDto addRentalPayment(@RequestBody RentalPaymentDto rentalPaymentDto) throws Exception {
		LOGGER.info("entering RentalPaymentController addRentalPayment - " + rentalPaymentDto.getStatementFromString() + ", " + rentalPaymentDto.getStatementToString() + ", " + rentalPaymentDto.getProperty());
		RentalPayment rentalPayment = rentalPaymentService.createRentalPayment(convertToEntity(rentalPaymentDto));
		LOGGER.info("leaving RentalPaymentController addRentalPayment - " + rentalPaymentDto.getStatementFromString() + ", " + rentalPaymentDto.getStatementToString() + ", " + rentalPaymentDto.getProperty());
		
		return convertToDto(rentalPayment);
    }
	
	@RequestMapping(value = "/rentalPayments/{id}", method = RequestMethod.PUT, produces = "application/json", 
			consumes = "application/json", headers = "Accept=application/json")
	RentalPaymentDto updateDonation(@RequestBody RentalPaymentDto rentalPaymentDto, Long id) throws Exception {
		LOGGER.info("entering RentalPaymentController updateRentalPayment - " + id);
		RentalPayment rentalPayment = rentalPaymentService.updateRentalPayment(convertToEntity(rentalPaymentDto));
		LOGGER.info("leaving RentalPaymentController updateRentalPayment - " + id);
		
		return convertToDto(rentalPayment);
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
		RentalPayment rentalPayment = rentalPaymentService.getRentalPayment(id);
		LOGGER.info("leaving RentalPaymentController getRentalPayment - " + id);
		
		return convertToDto(rentalPayment);
    }
	
	public RentalPaymentDto convertToDto(RentalPayment rentalPayment) throws Exception {
		return rentalPaymentMapperWrapper.rentalPaymentToRentalPaymentDto(rentalPayment);
	}
	
	public RentalPayment convertToEntity(RentalPaymentDto rentalPaymentDto) throws Exception {
		return rentalPaymentMapperWrapper.rentalPaymentDtoToRentalPayment(rentalPaymentDto);
	}
	
}
