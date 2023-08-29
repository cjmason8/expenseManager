package au.com.mason.expensemanager.util;

import au.com.mason.expensemanager.mapper.DocumentMapper;
import java.util.List;
import java.util.stream.Collectors;

import au.com.mason.expensemanager.config.SpringContext;
import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.dto.DocumentDto;

public class DocumentUtil {
	
	public static List<DocumentDto> convertList(List<Document> documents) {
		return documents.stream()
		          .map(document -> convertToDtoWrapper(document))
		          .collect(Collectors.toList());
	}
	
	private static DocumentDto convertToDtoWrapper(Document item) throws RuntimeException {
		try {
			return convertToDto(item);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static DocumentDto convertToDto(Document document) throws Exception {
	    return SpringContext.getApplicationContext().getBean(DocumentMapper.class).documentToDocumentDto(document);
	}
	
	public static Document convertToEntity(DocumentDto documentDto) throws Exception {
		return SpringContext.getApplicationContext().getBean(DocumentMapper.class).documentDtoToDocument(documentDto);
	}
}
