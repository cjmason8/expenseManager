package au.com.mason.expensemanager.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import au.com.mason.expensemanager.dao.RentalPaymentDao;
import au.com.mason.expensemanager.domain.RentalPayment;
import au.com.mason.expensemanager.util.RentalPaymentFinancialYear;
import au.com.mason.expensemanager.util.S3Keys;

@Service
public class RentalPaymentService {

	private static final Map<String, String> PROPERTIES = Map.of("WODONGA", "Wodonga", "STH_KINGSVILLE",
		"South Kingsville");

	private String docsRoot;
	private final RentalPaymentDao rentalPaymentDao;
	private final DocumentService documentService;

	public RentalPaymentService(@Value("${docs.location}") String docsRoot, RentalPaymentDao rentalPaymentDao,
		DocumentService documentService) {
		this.docsRoot = S3Keys.normalize(docsRoot);
		this.rentalPaymentDao = rentalPaymentDao;
		this.documentService = documentService;
	}

	public RentalPayment updateRentalPayment(RentalPayment rentalPayment) throws Exception {
		RentalPayment existingRentalPayment = rentalPaymentDao.getById(rentalPayment.getId());

		if (rentalPayment.getDocument() != null && rentalPayment.getDocument().getFileName() == null) {
			rentalPayment.setDocument(null);
		}

		if (rentalPayment.getDocument() != null && rentalPayment.getDocument().getOriginalFileName() != null && !Objects
			.equals(rentalPayment.getDocument().getFileName(), existingRentalPayment.getDocument().getFileName())) {
			moveAndUpdateDocument(rentalPayment);
		}

		rentalPaymentDao.update(rentalPayment);

		return rentalPayment;
	}

	public RentalPayment createRentalPayment(RentalPayment rentalPayment) throws Exception {
		if (rentalPayment.getDocument() != null && rentalPayment.getDocument().getOriginalFileName() != null) {
			moveAndUpdateDocument(rentalPayment);
		}

		rentalPaymentDao.create(rentalPayment);

		return rentalPayment;
	}

	private void moveAndUpdateDocument(RentalPayment rentalPayment) throws IOException {
		int year = rentalPayment.getStatementFrom().getYear();
		int month = rentalPayment.getStatementFrom().getMonth().getValue();
		String financialYear = (month <= 6) ? (year - 1) + "-" + year : year + "-" + (year + 1);
		String propertyName = PROPERTIES.get(rentalPayment.getProperty());

		String destParent = S3Keys.toUiFolderPath(
			String.format("/docs/expenseManager/filofax/IPs/%s/%s/Statements", propertyName, financialYear));

		documentService.moveDocumentToParentFolder(rentalPayment.getDocument(), destParent);
		rentalPayment.getDocument().setMetaData(Map.of("property", propertyName, "year", financialYear));

		documentService.updateDocument(rentalPayment.getDocument());
	}

	public void deleteRentalPayment(Long id) {
		rentalPaymentDao.deleteById(id);
	}

	public RentalPayment getRentalPayment(Long id) {
		return rentalPaymentDao.getById(id);
	}

	public List<RentalPayment> getAll(String property, int financialYearEnd) {
		return rentalPaymentDao.getByProperty(property).stream()
			.filter(payment -> RentalPaymentFinancialYear.financialYearEnd(payment) == financialYearEnd).toList();
	}

}
