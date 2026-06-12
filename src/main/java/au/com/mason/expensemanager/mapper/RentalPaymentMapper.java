package au.com.mason.expensemanager.mapper;

import au.com.mason.expensemanager.domain.RentalPayment;
import au.com.mason.expensemanager.dto.RentalPaymentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DocumentMapper.class, MappingConverters.class})
public interface RentalPaymentMapper extends BaseMapper<RentalPayment, RentalPaymentDto> {

	@Override
	@Mapping(source = "totalRent", target = "totalRent", qualifiedByName = "bigDecimalToString")
	@Mapping(source = "managementFee", target = "managementFee", qualifiedByName = "bigDecimalToString")
	@Mapping(source = "adminFee", target = "adminFee", qualifiedByName = "bigDecimalToString")
	@Mapping(source = "otherFee", target = "otherFee", qualifiedByName = "bigDecimalToString")
	@Mapping(source = "statementFrom", target = "statementFromString", qualifiedByName = "localDateToString")
	@Mapping(source = "statementTo", target = "statementToString", qualifiedByName = "localDateToString")
	@Mapping(source = "document", target = "documentDto")
	RentalPaymentDto entityToDto(RentalPayment rentalPayment);

	@Override
	@Mapping(source = "totalRent", target = "totalRent", qualifiedByName = "stringToBigDecimal")
	@Mapping(source = "managementFee", target = "managementFee", qualifiedByName = "stringToBigDecimal")
	@Mapping(source = "adminFee", target = "adminFee", qualifiedByName = "stringToBigDecimal")
	@Mapping(source = "otherFee", target = "otherFee", qualifiedByName = "stringToBigDecimal")
	@Mapping(source = "statementFromString", target = "statementFrom", qualifiedByName = "stringToLocalDate")
	@Mapping(source = "statementToString", target = "statementTo", qualifiedByName = "stringToLocalDate")
	@Mapping(source = "documentDto", target = "document", conditionQualifiedByName = "hasDocumentFileName")
	RentalPayment dtoToEntity(RentalPaymentDto rentalPaymentDto);

}
