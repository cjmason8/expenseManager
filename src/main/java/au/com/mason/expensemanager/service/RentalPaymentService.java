package au.com.mason.expensemanager.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import au.com.mason.expensemanager.dao.RentalPaymentDao;
import au.com.mason.expensemanager.domain.RentalPayment;
import au.com.mason.expensemanager.dto.RentalPaymentDto;
import au.com.mason.expensemanager.mapper.DocumentMapperWrapper;
import au.com.mason.expensemanager.mapper.RentalPaymentMapperWrapper;

@Component
public class RentalPaymentService {
	
	public static List<String> FIRST_SIX_MONTHS;
	public static Map<String, String> PROPERTIES;
	
	private Gson gson = new GsonBuilder().serializeNulls().create();
	
	@Autowired
	private RentalPaymentMapperWrapper rentalPaymentMapperWrapper;
	
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
		PROPERTIES.put("STH_KINGSVILLE", "Sth Kingsville");
	}
	
	public RentalPayment createRentalPayment(RentalPayment rentalPayment) throws Exception {
		
		rentalPaymentDao.create(rentalPayment);
		
		return rentalPayment;
	}
	
	public RentalPaymentDto updateRentalPayment(RentalPaymentDto rentalPaymentDto) throws Exception {
		
		if (rentalPaymentDto.getDocumentDto() != null && rentalPaymentDto.getDocumentDto().getFileName() == null) {
			rentalPaymentDto.setDocumentDto(null);
		}
		
		if (rentalPaymentDto.getDocumentDto() != null && rentalPaymentDto.getDocumentDto().getOriginalFileName() != null) {
			updateDocument(rentalPaymentDto);
		}
		
		RentalPayment updatedRentalPayment = rentalPaymentDao.getById(rentalPaymentDto.getId());
		updatedRentalPayment = rentalPaymentMapperWrapper.rentalPaymentDtoToRentalPayment(rentalPaymentDto, updatedRentalPayment);
		
		rentalPaymentDao.update(updatedRentalPayment);
		
		return rentalPaymentMapperWrapper.rentalPaymentToRentalPaymentDto(updatedRentalPayment);
	}
	
	public RentalPaymentDto createRentalPayment(RentalPaymentDto rentalPaymentDto) throws Exception {
		
		if (rentalPaymentDto.getDocumentDto() != null && rentalPaymentDto.getDocumentDto().getOriginalFileName() != null) {
			updateDocument(rentalPaymentDto);
		}
		
		RentalPayment rentalPayment = rentalPaymentMapperWrapper.rentalPaymentDtoToRentalPayment(rentalPaymentDto);
		rentalPaymentDao.create(rentalPayment);
		
		return rentalPaymentDto;
	}
	
	private void updateDocument(RentalPaymentDto rentalPaymentDto) throws IOException, Exception {
		
		String fileName = rentalPaymentDto.getDocumentDto().getFileName();
		int indexOf = fileName.indexOf(" (");
		if (indexOf == -1) {
			indexOf = fileName.indexOf(" [");
		}
		String folder = fileName.substring(fileName.indexOf(" to ") + 4, indexOf);
		String month = folder.substring(folder.indexOf(" "), folder.lastIndexOf(" "));
		String year = folder.substring(folder.lastIndexOf(" ") + 1);
		if (FIRST_SIX_MONTHS.contains(month)) {
			folder = (Integer.valueOf(year) - 1) + "-" + year; 
		}
		else {
			folder = year + "-" + (Integer.valueOf(year) + 1);
		}
		Map<String, String> metaData = new HashMap<>();
		metaData.put("property", PROPERTIES.get(rentalPaymentDto.getProperty()));
		metaData.put("year", folder);
		
		String folderPath = DocumentService.IP_FOLDER_PATH + "/" + PROPERTIES.get(rentalPaymentDto.getProperty())+ "/" + folder + "/Statements";
		Files.move(Paths.get(rentalPaymentDto.getDocumentDto().getFilePath()), Paths.get(folderPath + "/" + fileName));
		rentalPaymentDto.getDocumentDto().setFolderPath(folderPath);
		rentalPaymentDto.getDocumentDto().setMetaDataChunk(gson.toJson(metaData));
		
		documentService.updateDocument(rentalPaymentDto.getDocumentDto());
	}
	
	public void deleteRentalPayment(Long id) {
		rentalPaymentDao.deleteById(id);
	}
	
	public RentalPaymentDto getRentalPayment(Long id) throws Exception {
		RentalPayment rentalPayment = rentalPaymentDao.getById(id);
		
		return rentalPaymentMapperWrapper.rentalPaymentToRentalPaymentDto(rentalPayment);
	}
	
	public List<RentalPaymentDto> getAll(String property) throws Exception {
		List<RentalPaymentDto> rentalPaymentDtos = new ArrayList<>();
		for(RentalPayment rentalPayment : rentalPaymentDao.getAll(property)) {
			rentalPaymentDtos.add(rentalPaymentMapperWrapper.rentalPaymentToRentalPaymentDto(rentalPayment));
		};
		
		return rentalPaymentDtos;
	}
	
}
