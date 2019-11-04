package au.com.mason.expensemanager.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import au.com.mason.expensemanager.config.SpringContext;
import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.dto.DocumentDto;

public class DocumentUtil {
	
	private static Gson gson = new GsonBuilder().serializeNulls().create();

	public static List<DocumentDto> convertList(List<Document> documents) {
		return documents.stream()
		          .map(document -> convertToDto(document))
		          .collect(Collectors.toList());
	}
	
	public static DocumentDto convertToDto(Document document) {
		DocumentDto documentDto = SpringContext.getApplicationContext().getBean(ModelMapper.class).map(document, DocumentDto.class);
		if (document.getMetaData() != null) {
    		documentDto.setMetaDataChunk(gson.toJson(document.getMetaData(), Map.class));
    	}
    	documentDto.setOriginalFileName(documentDto.getFileName());
    	documentDto.setIsFolder(document.isFolder());
    	
	    return documentDto;
	}
	
	public static Document convertToEntity(DocumentDto documentDto) {
		Document document = SpringContext.getApplicationContext().getBean(ModelMapper.class).map(documentDto, Document.class);
		if (!StringUtils.isEmpty(documentDto.getMetaDataChunk())) {
			document.setMetaData((Map<String, Object>) gson.fromJson(documentDto.getMetaDataChunk(), Map.class));
		}
    	document.setFolder(documentDto.getIsFolder());
    	
	    return document;
	}
}
