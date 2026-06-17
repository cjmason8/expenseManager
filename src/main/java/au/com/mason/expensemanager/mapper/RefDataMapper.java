package au.com.mason.expensemanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.dto.RefDataDto;

@Mapper(componentModel = "spring", uses = MappingConverters.class)
public interface RefDataMapper extends BaseMapper<RefData, RefDataDto> {

	@Override
	@Mapping(source = "type", target = "type", qualifiedByName = "refDataTypeToString")
	@Mapping(source = "type", target = "typeDescription", qualifiedByName = "refDataTypeDescription")
	@Mapping(source = "metaData", target = "metaDataChunk", qualifiedByName = "stringMapToJson")
	@Mapping(target = "value", ignore = true)
	RefDataDto entityToDto(RefData refData);

	@Override
	@Mapping(source = "type", target = "type", qualifiedByName = "stringToRefDataType")
	@Mapping(source = "metaDataChunk", target = "metaData", qualifiedByName = "jsonToStringMap")
	@Mapping(target = "emailKey", ignore = true)
	@Mapping(target = "emailProcessor", ignore = true)
	RefData dtoToEntity(RefDataDto refDataDto);

}
