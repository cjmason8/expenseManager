package au.com.mason.expensemanager.controller;

import au.com.mason.expensemanager.domain.RentalPayment;
import au.com.mason.expensemanager.dto.RentalPaymentDto;
import au.com.mason.expensemanager.dto.RentalPaymentInfoDto;
import au.com.mason.expensemanager.dto.StatusResponseDto;
import au.com.mason.expensemanager.mapper.RentalPaymentMapper;
import au.com.mason.expensemanager.service.RentalPaymentService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RentalPaymentController extends BaseController<RentalPayment, RentalPaymentDto> {
	
	@Autowired
	private RentalPaymentService rentalPaymentService;
	
	private static final Logger LOGGER = LogManager.getLogger(RentalPaymentController.class);

	@Autowired
	public RentalPaymentController(RentalPaymentMapper rentalPaymentMapper) {
		super(rentalPaymentMapper);
	}
	
	@GetMapping(value = {"/rentalPayments/getByProperty/{property}/{year}", "/rentalPayments/getByProperty/{property}"}, produces = "application/json")
	RentalPaymentInfoDto getRentalPayments(@PathVariable String property, @PathVariable(required=false) Integer year) throws Exception {
		LOGGER.info("entering RentalPaymentController getRentalPayment");
		LocalDate startDate;
		LocalDate endDate;
		if (year == null) {
			if (LocalDate.now().getMonth().getValue() <= 6) {
				startDate = LocalDate.of(LocalDate.now().getYear() - 1, 7, 1);
				endDate = LocalDate.of(LocalDate.now().getYear(), 6, 30);
			}
			else {
				startDate = LocalDate.of(LocalDate.now().getYear(), 7, 1);
				endDate = LocalDate.of(LocalDate.now().getYear() + 1, 6, 30);
			}
		}
		else {
			startDate = LocalDate.of(year - 1, 7, 1);
			endDate = LocalDate.of(year, 6, 30);
		}
		
		List<RentalPayment> results = rentalPaymentService.getAll(property, startDate, endDate);
		
		Integer prevYear = null;
		Integer nextYear = null;
		if (rentalPaymentService.getAll(property, startDate.minus(1, ChronoUnit.YEARS), endDate.minus(1, ChronoUnit.YEARS)).size() > 0) {
			prevYear = endDate.getYear() - 1;
		}
		if (rentalPaymentService.getAll(property, startDate.plus(1, ChronoUnit.YEARS), endDate.plus(1, ChronoUnit.YEARS)).size() > 0) {
			nextYear = endDate.getYear() + 1;
		}
		LOGGER.info("leaving RentalPaymentController getRentalPayments");

		return new RentalPaymentInfoDto(convertList(results), prevYear, nextYear);
    }
	
	@PostMapping(value = "/rentalPayments", produces = "application/json", consumes = "application/json")
	RentalPaymentDto addRentalPayment(@RequestBody RentalPaymentDto rentalPaymentDto) throws Exception {
		LOGGER.info("entering RentalPaymentController addRentalPayment - " + rentalPaymentDto.getStatementFromString() + ", " + rentalPaymentDto.getStatementToString() + ", " + rentalPaymentDto.getProperty());
		RentalPayment rentalPayment = rentalPaymentService.createRentalPayment(convertToEntity(rentalPaymentDto));
		LOGGER.info("leaving RentalPaymentController addRentalPayment - " + rentalPaymentDto.getStatementFromString() + ", " + rentalPaymentDto.getStatementToString() + ", " + rentalPaymentDto.getProperty());
		
		return convertToDto(rentalPayment);
    }
	
	@PutMapping(value = "/rentalPayments/{id}", produces = "application/json",
			consumes = "application/json", headers = "Accept=application/json")
	RentalPaymentDto updateDonation(@RequestBody RentalPaymentDto rentalPaymentDto, Long id) throws Exception {
		LOGGER.info("entering RentalPaymentController updateRentalPayment - " + id);
		RentalPayment rentalPayment = rentalPaymentService.updateRentalPayment(convertToEntity(rentalPaymentDto));
		LOGGER.info("leaving RentalPaymentController updateRentalPayment - " + id);
		
		return convertToDto(rentalPayment);
    }
	
	
	@DeleteMapping(value = "/rentalPayments/{id}", produces = "application/json",
			consumes = "application/json", headers = "Accept=application/json")
	StatusResponseDto deleteRentalPayment(@PathVariable Long id) {
		LOGGER.info("entering RentalPaymentController deleteRentalPayment - " + id);
		rentalPaymentService.deleteRentalPayment(id);
		LOGGER.info("leaving RentalPaymentController deleteRentalPayment - " + id);
		
		return new StatusResponseDto("success");
    }
	
	@GetMapping(value = "/rentalPayments/{id}", produces = "application/json")
	RentalPaymentDto getRentalPayment(@PathVariable Long id) {
		LOGGER.info("entering RentalPaymentController getRentalPayment - " + id);
		RentalPayment rentalPayment = rentalPaymentService.getRentalPayment(id);
		LOGGER.info("leaving RentalPaymentController getRentalPayment - " + id);
		
		return convertToDto(rentalPayment);
    }
	
}
