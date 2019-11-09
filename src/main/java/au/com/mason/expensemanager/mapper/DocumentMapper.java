package au.com.mason.expensemanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.dto.DocumentDto;

@Component
@Mapper
public interface DocumentMapper {
	
	DocumentMapper INSTANCE = Mappers.getMapper( DocumentMapper.class );
	 
    Document documentDtoToDocument(DocumentDto documentDto) throws Exception;
    
    Document documentDtoToDocument(DocumentDto documentDto, @MappingTarget Document document) throws Exception;
    
    DocumentDto documentToDocumentDto(Document document) throws Exception;

}
