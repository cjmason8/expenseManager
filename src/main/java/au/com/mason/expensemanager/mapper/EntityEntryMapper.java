package au.com.mason.expensemanager.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import au.com.mason.expensemanager.domain.EntityEntry;
import au.com.mason.expensemanager.dto.EntityEntryDto;
import au.com.mason.expensemanager.util.EntityEntryDataFields;

@Mapper(componentModel = "spring", uses = {DocumentMapper.class, MappingConverters.class})
public abstract class EntityEntryMapper implements BaseMapper<EntityEntry, EntityEntryDto> {

	@Override
	@Mapping(source = "type", target = "type", qualifiedByName = "entityTypeToString")
	@Mapping(source = "metaData", target = "metaDataChunk", qualifiedByName = "stringMapToJson")
	@Mapping(source = "data", target = "dataChunk", qualifiedByName = "objectMapToJson")
	@Mapping(source = "document", target = "documentDto")
	@Mapping(target = "link", ignore = true)
	public abstract EntityEntryDto entityToDto(EntityEntry entityEntry);

	@Override
	@Mapping(source = "type", target = "type", qualifiedByName = "stringToEntityType")
	@Mapping(source = "metaDataChunk", target = "metaData", qualifiedByName = "jsonToStringMap")
	@Mapping(source = "dataChunk", target = "data", qualifiedByName = "jsonToObjectMap")
	@Mapping(source = "documentDto", target = "document", conditionQualifiedByName = "hasDocumentFileName")
	public abstract EntityEntry dtoToEntity(EntityEntryDto entityEntryDto);

	@AfterMapping
	protected void flattenTypeSpecificFields(EntityEntry source, @MappingTarget EntityEntryDto target) {
		EntityEntryDataFields.flattenToDto(source.getType(), source.getData(), target);
	}

	@AfterMapping
	protected void mergeTypeSpecificFields(EntityEntryDto source, @MappingTarget EntityEntry target) {
		EntityEntryDataFields.mergeFromDto(source, target);
	}

}
