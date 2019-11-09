package au.com.mason.expensemanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.RentalPayment;
import au.com.mason.expensemanager.dto.RentalPaymentDto;

@Component
@Mapper
public interface RentalPaymentMapper {
	
	RentalPaymentMapper INSTANCE = Mappers.getMapper( RentalPaymentMapper.class );
	 
    RentalPayment rentalPaymentDtoToRentalPayment(RentalPaymentDto rentalPaymentDto) throws Exception;
    
    RentalPayment rentalPaymentDtoToRentalPayment(RentalPaymentDto rentalPaymentDto, @MappingTarget RentalPayment rentalPayment) throws Exception;
    
    RentalPaymentDto rentalPaymentToRentalPaymentDto(RentalPayment rentalPayment) throws Exception;

}
