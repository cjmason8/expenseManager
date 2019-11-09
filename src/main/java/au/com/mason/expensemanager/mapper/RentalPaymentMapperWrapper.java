package au.com.mason.expensemanager.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import au.com.mason.expensemanager.domain.RentalPayment;
import au.com.mason.expensemanager.dto.RentalPaymentDto;
import au.com.mason.expensemanager.util.DateUtil;
import au.com.mason.expensemanager.util.DocumentUtil;

@Component
public class RentalPaymentMapperWrapper {
	
	private RentalPaymentMapper rentalPaymentMapper = RentalPaymentMapper.INSTANCE;
	private Gson gson = new GsonBuilder().serializeNulls().create();
	
	public RentalPayment rentalPaymentDtoToRentalPayment(RentalPaymentDto rentalPaymentDto) throws Exception {
		RentalPayment rentalPayment = rentalPaymentMapper.rentalPaymentDtoToRentalPayment(rentalPaymentDto);
		rentalPayment.setStatementFrom(DateUtil.getFormattedDate(rentalPaymentDto.getStatementFromString()));
		rentalPayment.setStatementTo(DateUtil.getFormattedDate(rentalPaymentDto.getStatementToString()));
		if (rentalPaymentDto.getDocumentDto() != null) {
			rentalPayment.setDocument(DocumentUtil.convertToEntity(rentalPaymentDto.getDocumentDto()));
		}
		
		return rentalPayment;
	}

    public RentalPayment rentalPaymentDtoToRentalPayment(RentalPaymentDto rentalPaymentDto, RentalPayment rentalPaymentParam) throws Exception {
    	RentalPayment rentalPayment = rentalPaymentMapper.rentalPaymentDtoToRentalPayment(rentalPaymentDto, rentalPaymentParam);
    	rentalPayment.setStatementFrom(DateUtil.getFormattedDate(rentalPaymentDto.getStatementFromString()));
		rentalPayment.setStatementTo(DateUtil.getFormattedDate(rentalPaymentDto.getStatementToString()));
		if (rentalPaymentDto.getDocumentDto() != null) {
			rentalPayment.setDocument(DocumentUtil.convertToEntity(rentalPaymentDto.getDocumentDto()));
		}
    	
		return rentalPayment;
    }
    
    public RentalPaymentDto rentalPaymentToRentalPaymentDto(RentalPayment rentalPayment) throws Exception {
    	RentalPaymentDto rentalPaymentDto = rentalPaymentMapper.rentalPaymentToRentalPaymentDto(rentalPayment);
    	rentalPaymentDto.setStatementFromString(DateUtil.getFormattedDateString(rentalPayment.getStatementFrom()));
    	rentalPaymentDto.setStatementToString(DateUtil.getFormattedDateString(rentalPayment.getStatementTo()));
		if (rentalPayment.getDocument() != null) {
			rentalPaymentDto.setDocumentDto(DocumentUtil.convertToDto(rentalPayment.getDocument()));
		}

		return rentalPaymentDto;
    }

}
