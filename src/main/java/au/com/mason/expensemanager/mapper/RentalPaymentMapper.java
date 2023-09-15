package au.com.mason.expensemanager.mapper;

import au.com.mason.expensemanager.domain.RentalPayment;
import au.com.mason.expensemanager.dto.RentalPaymentDto;
import au.com.mason.expensemanager.util.DateUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RentalPaymentMapper implements BaseMapper<RentalPayment, RentalPaymentDto> {
    @Autowired
    private DocumentMapper documentMapper;

    public RentalPayment dtoToEntity(RentalPaymentDto rentalPaymentDto) {
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
        rentalPayment.setStatementFrom(DateUtil.getFormattedDate(rentalPaymentDto.getStatementFromString()));
        rentalPayment.setStatementTo(DateUtil.getFormattedDate(rentalPaymentDto.getStatementToString()));
        if (rentalPaymentDto.getDocumentDto() != null && rentalPaymentDto.getDocumentDto().getFileName() != null) {
            rentalPayment.setDocument(documentMapper.dtoToEntity(rentalPaymentDto.getDocumentDto()));
        }

        return rentalPayment;
    }

    public RentalPaymentDto entityToDto(RentalPayment rentalPayment) {
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
        rentalPaymentDto.setStatementFromString(DateUtil.getFormattedDateString(rentalPayment.getStatementFrom()));
        rentalPaymentDto.setStatementToString(DateUtil.getFormattedDateString(rentalPayment.getStatementTo()));
        if (rentalPayment.getDocument() != null) {
            rentalPaymentDto.setDocumentDto(documentMapper.entityToDto(rentalPayment.getDocument()));
        }

        return rentalPaymentDto;
    }
}
