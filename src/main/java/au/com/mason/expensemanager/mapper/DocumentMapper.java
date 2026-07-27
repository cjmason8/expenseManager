package au.com.mason.expensemanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.dto.DocumentDto;

@Mapper(componentModel = "spring", uses = MappingConverters.class)
public interface DocumentMapper extends BaseMapper<Document, DocumentDto> {

	@Override
	@Mapping(source = "folder", target = "isFolder")
	@Mapping(source = "metaData", target = "metaDataChunk", qualifiedByName = "objectMapToJson")
	@Mapping(source = "archived", target = "isArchived")
	DocumentDto entityToDto(Document document);

	@Override
	@Mapping(source = "isFolder", target = "folder")
	@Mapping(source = "metaDataChunk", target = "metaData", qualifiedByName = "jsonToObjectMap")
	@Mapping(source = "isArchived", target = "archived")
	Document dtoToEntity(DocumentDto documentDto);

}
