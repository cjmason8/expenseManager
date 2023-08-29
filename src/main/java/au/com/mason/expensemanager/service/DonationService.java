package au.com.mason.expensemanager.service;

import au.com.mason.expensemanager.domain.Donation;
import au.com.mason.expensemanager.domain.Statics;
import au.com.mason.expensemanager.dto.DonationSearchDto;
import au.com.mason.expensemanager.repository.CustomRepository;
import au.com.mason.expensemanager.repository.DonationRepository;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DonationService extends BaseService<Donation> {

	@Autowired
	private DonationRepository donationRepository;

	@Autowired
	private CustomRepository customRepository;
	
	@Autowired
	protected DocumentService documentService;

	protected DonationService() {
		super(Donation.class);
	}

	public Donation updateDonation(Donation donation) throws Exception {
		
		updateDocument(donation);
		
		return donationRepository.save(donation);
	}
	
	public Donation createDonation(Donation donation) throws Exception {
		
		if (donation.getDocument() != null && donation.getDocument().getOriginalFileName() != null) {
			updateDocument(donation);
		}
		else {
			donation.setDocument(null);
		}
		
		return donationRepository.save(donation);
	}
	
	private void updateDocument(Donation donation) throws Exception {
		if (!donation.getDocument().getOriginalFileName().equals(donation.getDocument().getFileName())) {
			Files.move(Paths.get(donation.getDocument().getFolderPath() + "/" + donation.getDocument().getOriginalFileName()),
					Paths.get(donation.getDocument().getFolderPath() + "/" + donation.getDocument().getFileName()));
			
			documentService.updateDocument(donation.getDocument());
		}
	}
	
	public void deleteDonation(Long id) {
		donationRepository.deleteById(id);
	}
	
	public Donation getById(Long id) throws Exception {
		return findById(donationRepository, id);
	}

	public List<Donation> getAll() {
		var results = new ArrayList<Donation>();
		donationRepository.findAll().forEach(results::add);
		results.sort(Comparator.comparing(Donation::getDescription).reversed());

		return results.stream().limit(Statics.MAX_RESULTS.getIntValue()).collect(Collectors.toList());
	}

	public List<Donation> findDonations(DonationSearchDto donationSearchDto) {
		return customRepository.findDonations(donationSearchDto);
	}
	
}
