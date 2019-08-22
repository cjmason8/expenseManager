package au.com.mason.expensemanager.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.dao.RentalPaymentDao;
import au.com.mason.expensemanager.domain.RentalPayment;
import au.com.mason.expensemanager.dto.RentalPaymentDto;
import au.com.mason.expensemanager.mapper.RentalPaymentMapperWrapper;

@Component
public class RentalPaymentService {
	
	@Autowired
	private RentalPaymentMapperWrapper rentalPaymentMapperWrapper;
	
	@Autowired
	private RentalPaymentDao rentalPaymentDao;
	
	@Autowired
	protected DocumentService documentService;
	
	public RentalPayment createRentalPayment(RentalPayment rentalPayment) throws Exception {
		
		rentalPaymentDao.create(rentalPayment);
		
		return rentalPayment;
	}
	
	public RentalPaymentDto updateRentalPayment(RentalPaymentDto rentalPaymentDto) throws Exception {
		
		updateDocument(rentalPaymentDto);
		
		RentalPayment updatedRentalPayment = rentalPaymentDao.getById(rentalPaymentDto.getId());
		updatedRentalPayment = rentalPaymentMapperWrapper.rentalPaymentDtoToRentalPayment(rentalPaymentDto, updatedRentalPayment);
		
		rentalPaymentDao.update(updatedRentalPayment);
		
		return rentalPaymentMapperWrapper.rentalPaymentToRentalPaymentDto(updatedRentalPayment);
	}
	
	public RentalPaymentDto createRentalPayment(RentalPaymentDto rentalPaymentDto) throws Exception {
		
		if (rentalPaymentDto.getDocumentDto() != null && rentalPaymentDto.getDocumentDto().getOriginalFileName() != null) {
			updateDocument(rentalPaymentDto);
		}
		else {
			rentalPaymentDto.setDocumentDto(null);
		}
		
		RentalPayment rentalPayment = rentalPaymentMapperWrapper.rentalPaymentDtoToRentalPayment(rentalPaymentDto);
		
		rentalPaymentDao.create(rentalPayment);
		
		return rentalPaymentDto;
	}
	
	private void updateDocument(RentalPaymentDto rentalPaymentDto) throws IOException, Exception {
		if (!rentalPaymentDto.getDocumentDto().getOriginalFileName().equals(rentalPaymentDto.getDocumentDto().getFileName())) {
			Files.move(Paths.get(rentalPaymentDto.getDocumentDto().getFolderPath() + "/" + rentalPaymentDto.getDocumentDto().getOriginalFileName()),
					Paths.get(rentalPaymentDto.getDocumentDto().getFolderPath() + "/" + rentalPaymentDto.getDocumentDto().getFileName()));
			
			documentService.updateDocument(rentalPaymentDto.getDocumentDto());
		}
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
