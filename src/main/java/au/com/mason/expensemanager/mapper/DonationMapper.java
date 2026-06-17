package au.com.mason.expensemanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import au.com.mason.expensemanager.domain.Donation;
import au.com.mason.expensemanager.dto.DonationDto;

@Mapper(componentModel = "spring", uses = {RefDataMapper.class, DocumentMapper.class, MappingConverters.class})
public interface DonationMapper extends BaseMapper<Donation, DonationDto> {

	@Override
	@Mapping(source = "dueDate", target = "dueDateString", qualifiedByName = "localDateToString")
	@Mapping(source = "dueDate", target = "dueDate")
	@Mapping(source = "metaData", target = "metaDataChunk", qualifiedByName = "stringMapToJson")
	@Mapping(source = "document", target = "documentDto")
	DonationDto entityToDto(Donation donation);

	@Override
	@Mapping(source = "dueDateString", target = "dueDate", qualifiedByName = "stringToLocalDate")
	@Mapping(source = "metaDataChunk", target = "metaData", qualifiedByName = "jsonToStringMap")
	@Mapping(source = "documentDto", target = "document", conditionQualifiedByName = "hasDocumentFileName")
	Donation dtoToEntity(DonationDto donationDto);

}
