package au.com.mason.expensemanager.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.dao.DonationDao;
import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.Donation;
import au.com.mason.expensemanager.dto.DonationSearchDto;

@Component
public class DonationService {

	@Autowired
	private DonationDao donationDao;

	@Autowired
	protected DocumentService documentService;

	public Donation updateDonation(Donation donation) throws Exception {

		// updateDocument(donation);

		return donationDao.update(donation);
	}

	public Donation createDonation(Donation donation) throws Exception {

		if (donation.getDocument() != null && isDocumentAttached(donation.getDocument())) {
			donation.setDocument(documentService.getById(donation.getDocument().getId()));
		} else {
			donation.setDocument(null);
		}

		return donationDao.create(donation);
	}

	private void updateDocument(Donation donation) throws Exception {
		if (donation.getDocument() != null && donation.getDocument().getFileName() != null
			&& donation.getDocument().getOriginalFileName() != null
			&& !donation.getDocument().getOriginalFileName().equals(donation.getDocument().getFileName())) {
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

	private static boolean isDocumentAttached(Document doc) {
		return doc != null && (doc.getId() != null || StringUtils.isNotBlank(doc.getFileName()));
	}

}
