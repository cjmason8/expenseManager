package au.com.mason.expensemanager.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.dao.DonationDao;
import au.com.mason.expensemanager.domain.Donation;
import au.com.mason.expensemanager.dto.DonationSearchDto;

@Component
public class DonationService {
	
	@Autowired
	private DonationDao donationDao;
	
	@Autowired
	protected DocumentService documentService;
	
	public Donation updateDonation(Donation donation) throws Exception {
		
		updateDocument(donation);
		
		return donationDao.update(donation);
	}
	
	public Donation createDonation(Donation donation) throws Exception {
		
		if (donation.getDocument() != null && donation.getDocument().getOriginalFileName() != null) {
			updateDocument(donation);
		}
		else {
			donation.setDocument(null);
		}
		
		return donationDao.create(donation);
	}
	
	private void updateDocument(Donation donation) throws IOException, Exception {
		if (!donation.getDocument().getOriginalFileName().equals(donation.getDocument().getFileName())) {
			Files.move(Paths.get(donation.getDocument().getFolderPath() + "/" + donation.getDocument().getOriginalFileName()),
					Paths.get(donation.getDocument().getFolderPath() + "/" + donation.getDocument().getFileName()));
			
			documentService.updateDocument(donation.getDocument());
		}
	}
	
	public void deleteDonation(Long id) {
		donationDao.deleteById(id);
	}
	
	public Donation getById(Long id) throws Exception {
		return donationDao.getById(id);
	}
	
	public List<Donation> getAll() throws Exception {
		return donationDao.getAll();
	}
	
	public List<Donation> findDonations(DonationSearchDto donationSearchDto) throws Exception {
		return donationDao.findDonations(donationSearchDto);
	}
	
}
