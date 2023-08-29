package au.com.mason.expensemanager.mapper;

import au.com.mason.expensemanager.domain.RentalPayment;
import au.com.mason.expensemanager.dto.RentalPaymentDto;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class RentalPaymentMapper {

    public RentalPayment rentalPaymentDtoToRentalPayment(RentalPaymentDto rentalPaymentDto) {
        if ( rentalPaymentDto == null ) {
            return null;
        }

        RentalPayment rentalPayment = new RentalPayment();

        if ( rentalPaymentDto.getId() != null ) {
            rentalPayment.setId( rentalPaymentDto.getId() );
        }
        if ( rentalPaymentDto.getManagementFee() != null ) {
            rentalPayment.setManagementFee( new BigDecimal( rentalPaymentDto.getManagementFee() ) );
        }
        if ( rentalPaymentDto.getAdminFee() != null ) {
            rentalPayment.setAdminFee( new BigDecimal( rentalPaymentDto.getAdminFee() ) );
        }
        if ( rentalPaymentDto.getOtherFee() != null ) {
            rentalPayment.setOtherFee( new BigDecimal( rentalPaymentDto.getOtherFee() ) );
        }
        if ( rentalPaymentDto.getTotalRent() != null ) {
            rentalPayment.setTotalRent( new BigDecimal( rentalPaymentDto.getTotalRent() ) );
        }
        rentalPayment.setProperty( rentalPaymentDto.getProperty() );

        return rentalPayment;
    }
    public RentalPaymentDto rentalPaymentToRentalPaymentDto(RentalPayment rentalPayment) {
        if ( rentalPayment == null ) {
            return null;
        }

        RentalPaymentDto rentalPaymentDto = new RentalPaymentDto();

        rentalPaymentDto.setId( rentalPayment.getId() );
        rentalPaymentDto.setProperty( rentalPayment.getProperty() );
        if ( rentalPayment.getTotalRent() != null ) {
            rentalPaymentDto.setTotalRent( rentalPayment.getTotalRent().toString() );
        }
        if ( rentalPayment.getManagementFee() != null ) {
            rentalPaymentDto.setManagementFee( rentalPayment.getManagementFee().toString() );
        }
        if ( rentalPayment.getAdminFee() != null ) {
            rentalPaymentDto.setAdminFee( rentalPayment.getAdminFee().toString() );
        }
        if ( rentalPayment.getOtherFee() != null ) {
            rentalPaymentDto.setOtherFee( rentalPayment.getOtherFee().toString() );
        }

        return rentalPaymentDto;
    }
}
