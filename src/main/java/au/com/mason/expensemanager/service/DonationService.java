package au.com.mason.expensemanager.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.dao.DonationDao;
import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.Donation;
import au.com.mason.expensemanager.domain.EntityMetadataType;
import au.com.mason.expensemanager.dto.DonationSearchDto;

@Component
public class DonationService {

	@Autowired
	private DonationDao donationDao;

	@Autowired
	protected DocumentService documentService;

	@Autowired
	private EntityMetadataService entityMetadataService;

	public Donation updateDonation(Donation donation) throws Exception {
		Donation updated = donationDao.update(donation);
		persistMetadata(updated);
		hydrateDonation(updated);
		return updated;
	}

	public Donation createDonation(Donation donation) throws Exception {

		if (donation.getDocument() != null && isDocumentAttached(donation.getDocument())) {
			donation.setDocument(documentService.getById(donation.getDocument().getId()));
		} else {
			donation.setDocument(null);
		}

		Donation created = donationDao.create(donation);
		persistMetadata(created);
		hydrateDonation(created);
		return created;
	}

	public void deleteDonation(Long id) {
		entityMetadataService.deleteForEntity(EntityMetadataType.DONATION, String.valueOf(id));
		donationDao.deleteById(id);
	}

	public Donation getById(Long id) throws Exception {
		Donation donation = donationDao.getById(id);
		hydrateDonation(donation);
		return donation;
	}

	public List<Donation> getAll() throws Exception {
		List<Donation> results = donationDao.getAll();
		hydrateDonations(results);
		return results;
	}

	public List<Donation> findDonations(DonationSearchDto donationSearchDto) throws Exception {
		List<Donation> results = donationDao.findDonations(donationSearchDto);
		hydrateDonations(results);
		return results;
	}

	private void hydrateDonation(Donation donation) {
		if (donation == null) {
			return;
		}
		hydrateDonations(List.of(donation));
	}

	private void hydrateDonations(List<Donation> donations) {
		entityMetadataService.hydrateList(EntityMetadataType.DONATION, donations, d -> String.valueOf(d.getId()),
			(entity, entityMetadata, objectMap, stringMap) -> {
				entity.setEntityMetadata(entityMetadata);
				entity.setMetaData(stringMap);
			});
		for (Donation donation : donations) {
			if (donation.getDocument() != null) {
				documentService.hydrateDocument(donation.getDocument());
			}
		}
	}

	private void persistMetadata(Donation donation) {
		if (donation == null || donation.getId() == 0) {
			return;
		}
		entityMetadataService.replace(EntityMetadataType.DONATION, String.valueOf(donation.getId()),
			donation.getMetaData());
	}

	private static boolean isDocumentAttached(Document doc) {
		return doc != null && (doc.getId() != null || StringUtils.isNotBlank(doc.getFileName()));
	}

}
