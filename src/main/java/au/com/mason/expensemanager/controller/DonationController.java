package au.com.mason.expensemanager.controller;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import au.com.mason.expensemanager.config.SpringContext;
import au.com.mason.expensemanager.domain.Donation;
import au.com.mason.expensemanager.dto.DonationDto;
import au.com.mason.expensemanager.dto.DonationSearchDto;
import au.com.mason.expensemanager.service.DonationService;
import au.com.mason.expensemanager.util.DateUtil;
import au.com.mason.expensemanager.util.DocumentUtil;

@RestController
public class DonationController extends BaseController<DonationDto, Donation> {
	
	@Autowired
	private DonationService donationService;
	
	private static Logger LOGGER = LogManager.getLogger(DonationController.class);
	private static Gson gson = new GsonBuilder().serializeNulls().create();
	
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
	
	@RequestMapping(value = "/donations/{id}", method = RequestMethod.PUT, produces = "application/json", 
			consumes = "application/json", headers = "Accept=application/json")
	DonationDto updateDonation(@RequestBody DonationDto donationDto, Long id) throws Exception {
		LOGGER.info("entering DonationController updateDonation - " + id);
		Donation donation = donationService.updateDonation(convertToEntity(donationDto));
		LOGGER.info("leaving DonationController updateDonation - " + id);
		
		return convertToDto(donation);
    }
	
	@RequestMapping(value = "/donations/{id}", method = RequestMethod.GET, produces = "application/json")
	DonationDto getDonation(@PathVariable Long id) throws Exception {
		LOGGER.info("entering DonationController getDonation - " + id);
		Donation donation = donationService.getById(id);
		LOGGER.info("leaving DonationController getDonation - " + id);
		
		return convertToDto(donation);
        
    }
	
	@RequestMapping(value = "/donations/{id}", method = RequestMethod.DELETE, produces = "application/json",
			consumes = "application/json", headers = "Accept=application/json")
	String deleteDonation(@PathVariable Long id) throws Exception {
		LOGGER.info("entering DonationController deleteDonation - " + id);
		donationService.deleteDonation(id);
		LOGGER.info("leaving DonationController deleteDonation - " + id);
		
		return "{\"status\":\"success\"}";
    }
	
	@RequestMapping(value = "/donations/search", method = RequestMethod.POST, produces = "application/json", 
			consumes = "application/json", headers = "Accept=application/json")
	List<DonationDto> findDonations(@RequestBody DonationSearchDto donationSearchDto) throws Exception {
		LOGGER.info("entering DonationController findDonations - " + donationSearchDto.getCause());
		List<Donation> donations = donationService.findDonations(donationSearchDto);
		LOGGER.info("leaving DonationController findDonations - " + donationSearchDto.getCause());
		
		return convertList(donations);
    }
	
	public DonationDto convertToDto(Donation donation) {
		DonationDto donationDto = SpringContext.getApplicationContext().getBean(ModelMapper.class).map(donation, DonationDto.class);
		donationDto.setDueDateString(DateUtil.getFormattedDateString(donation.getDueDate()));
    	donationDto.setMetaDataChunk(gson.toJson(donation.getMetaData(), Map.class));
		if (donation.getDocument() != null) {
			donationDto.setDocumentDto(DocumentUtil.convertToDto(donation.getDocument()));
		}
    	
	    return donationDto;
	}
	
	public Donation convertToEntity(DonationDto donationDto) {
		Donation donation = SpringContext.getApplicationContext().getBean(ModelMapper.class).map(donationDto, Donation.class);
		donation.setDueDate(DateUtil.getFormattedDate(donationDto.getDueDateString()));
    	donation.setMetaData((Map<String, String>) gson.fromJson(donationDto.getMetaDataChunk(), Map.class));
		if (donationDto.getDocumentDto() != null) {
			donation.setDocument(DocumentUtil.convertToEntity(donationDto.getDocumentDto()));
		}
    	
	    return donation;
	}
	
}
