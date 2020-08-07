package au.com.mason.expensemanager.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.dao.RentalPaymentDao;
import au.com.mason.expensemanager.domain.RentalPayment;

@Component
public class RentalPaymentService {
	
	public static List<String> FIRST_SIX_MONTHS;
	public static Map<String, String> PROPERTIES;
	
	@Autowired
	private RentalPaymentDao rentalPaymentDao;
	
	@Autowired
	protected DocumentService documentService;
	
	static {
		FIRST_SIX_MONTHS = new ArrayList<>();
		FIRST_SIX_MONTHS.add("Jan");
		FIRST_SIX_MONTHS.add("Feb");
		FIRST_SIX_MONTHS.add("Mar");
		FIRST_SIX_MONTHS.add("Apr");
		FIRST_SIX_MONTHS.add("May");
		FIRST_SIX_MONTHS.add("Jun");
		
		PROPERTIES = new HashMap<>();
		PROPERTIES.put("WODONGA", "Wodonga");
		PROPERTIES.put("STH_KINGSVILLE", "South Kingsville");
	}
	
	public RentalPayment updateRentalPayment(RentalPayment rentalPayment) throws Exception {
		
		if (rentalPayment.getDocument() != null && rentalPayment.getDocument().getFileName() == null) {
			rentalPayment.setDocument(null);
		}
		
		if (rentalPayment.getDocument() != null && rentalPayment.getDocument().getOriginalFileName() != null) {
			updateDocument(rentalPayment);
		}
		
		rentalPaymentDao.update(rentalPayment);
		
		return rentalPayment;
	}
	
	public RentalPayment createRentalPayment(RentalPayment rentalPayment) throws Exception {
		
		if (rentalPayment.getDocument() != null && rentalPayment.getDocument().getOriginalFileName() != null) {
			updateDocument(rentalPayment);
		}
		
		rentalPaymentDao.create(rentalPayment);
		
		return rentalPayment;
	}
	
	private void updateDocument(RentalPayment rentalPayment) throws IOException, Exception {
		
		int month = rentalPayment.getStatementFrom().getMonth().getValue();
		String year = String.valueOf(rentalPayment.getStatementFrom().getYear());
		String folder = "";
		if (month <= 6) {
			folder = (Integer.valueOf(year) - 1) + "-" + year; 
		}
		else {
			folder = year + "-" + (Integer.valueOf(year) + 1);
		}
		Map<String, Object> metaData = new HashMap<>();
		metaData.put("property", PROPERTIES.get(rentalPayment.getProperty()));
		metaData.put("year", folder);
		
		String folderPath = DocumentService.IP_FOLDER_PATH + "/" + PROPERTIES.get(rentalPayment.getProperty())+ "/" + folder + "/Statements";
		Files.move(Paths.get(rentalPayment.getDocument().getFilePath()), Paths.get(folderPath + "/" + rentalPayment.getDocument().getFileName()));
		rentalPayment.getDocument().setFolderPath(folderPath);
		rentalPayment.getDocument().setMetaData(metaData);
		
		documentService.updateDocument(rentalPayment.getDocument());
	}
	
	public void deleteRentalPayment(Long id) {
		rentalPaymentDao.deleteById(id);
	}
	
	public RentalPayment getRentalPayment(Long id) throws Exception {
		return rentalPaymentDao.getById(id);
	}
	
	public List<RentalPayment> getAll(String property, LocalDate startDate, LocalDate endDate) throws Exception {
		return rentalPaymentDao.getAll(property, startDate, endDate);
	}
	
}
