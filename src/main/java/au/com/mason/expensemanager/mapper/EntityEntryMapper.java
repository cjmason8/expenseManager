package au.com.mason.expensemanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import au.com.mason.expensemanager.domain.EntityEntry;
import au.com.mason.expensemanager.dto.EntityEntryDto;

@Mapper(componentModel = "spring", uses = {DocumentMapper.class, MappingConverters.class})
public interface EntityEntryMapper extends BaseMapper<EntityEntry, EntityEntryDto> {

	@Override
	@Mapping(source = "type", target = "type", qualifiedByName = "entityTypeToString")
	@Mapping(source = "metaData", target = "metaDataChunk", qualifiedByName = "stringMapToJson")
	@Mapping(source = "document", target = "documentDto")
	EntityEntryDto entityToDto(EntityEntry entityEntry);

	@Override
	@Mapping(source = "type", target = "type", qualifiedByName = "stringToEntityType")
	@Mapping(source = "metaDataChunk", target = "metaData", qualifiedByName = "jsonToStringMap")
	@Mapping(source = "documentDto", target = "document", conditionQualifiedByName = "hasDocumentFileName")
	EntityEntry dtoToEntity(EntityEntryDto entityEntryDto);

}
