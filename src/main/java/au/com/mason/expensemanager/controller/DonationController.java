package au.com.mason.expensemanager.controller;

import au.com.mason.expensemanager.domain.Donation;
import au.com.mason.expensemanager.dto.DonationDto;
import au.com.mason.expensemanager.dto.DonationSearchDto;
import au.com.mason.expensemanager.dto.StatusResponseDto;
import au.com.mason.expensemanager.mapper.DonationMapper;
import au.com.mason.expensemanager.service.DonationService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DonationController extends BaseController<Donation, DonationDto> {
	
	@Autowired
	private DonationService donationService;

	@Autowired
	public DonationController(DonationMapper donationMapper) {
		super(donationMapper);
	}
	
	private static final Logger LOGGER = LogManager.getLogger(DonationController.class);

	@RequestMapping(value = "/donations", method = RequestMethod.GET, produces = "application/json")
	List<DonationDto> getDonations() throws Exception {
		LOGGER.info("entering DonationController getDonations");
		List<Donation> results = donationService.getAll();
		LOGGER.info("leaving DonationController getDonations");
		
		return convertList(results);
    }
	
	@PostMapping(value = "/donations", produces = "application/json", consumes = "application/json")
	DonationDto addDonation(@RequestBody DonationDto donationDto) throws Exception {
		LOGGER.info("entering DonationController addDonation - " + donationDto.getDescription());
		Donation donation = donationService.createDonation(convertToEntity(donationDto));
		LOGGER.info("leaving DonationController addDonation - " + donationDto.getDescription());
		
		return convertToDto(donation);
    }
	
	@PutMapping(value = "/donations/{id}", produces = "application/json",
			consumes = "application/json", headers = "Accept=application/json")
	DonationDto updateDonation(@RequestBody DonationDto donationDto, Long id) throws Exception {
		LOGGER.info("entering DonationController updateDonation - " + id);
		Donation donation = donationService.updateDonation(convertToEntity(donationDto));
		LOGGER.info("leaving DonationController updateDonation - " + id);
		
		return convertToDto(donation);
    }
	
	@GetMapping(value = "/donations/{id}", produces = "application/json")
	DonationDto getDonation(@PathVariable Long id) throws Exception {
		LOGGER.info("entering DonationController getDonation - " + id);
		Donation donation = donationService.getById(id);
		LOGGER.info("leaving DonationController getDonation - " + id);
		
		return convertToDto(donation);
        
    }
	
	@DeleteMapping(value = "/donations/{id}", produces = "application/json",
			headers = "Accept=application/json")
	StatusResponseDto deleteDonation(@PathVariable Long id) throws Exception {
		LOGGER.info("entering DonationController deleteDonation - " + id);
		donationService.deleteDonation(id);
		LOGGER.info("leaving DonationController deleteDonation - " + id);

		return new StatusResponseDto("success");
    }
	
	@PostMapping(value = "/donations/search", produces = "application/json",
			consumes = "application/json", headers = "Accept=application/json")
	List<DonationDto> findDonations(@RequestBody DonationSearchDto donationSearchDto) throws Exception {
		LOGGER.info("entering DonationController findDonations - " + donationSearchDto.getCause());
		List<Donation> donations = donationService.findDonations(donationSearchDto);
		LOGGER.info("leaving DonationController findDonations - " + donationSearchDto.getCause());
		
		return convertList(donations);
    }
	
}
