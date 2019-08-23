package au.com.mason.expensemanager.mapper;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.dto.DocumentDto;

@Component
public class DocumentMapperWrapper {
	
	private DocumentMapper documentMapper = DocumentMapper.INSTANCE;
	private Gson gson = new GsonBuilder().serializeNulls().create();
	
	public Document documentDtoToDocument(DocumentDto documentDto) throws Exception {
		Document document = documentMapper.documentDtoToDocument(documentDto);
		if (!StringUtils.isEmpty(documentDto.getMetaDataChunk())) {
			document.setMetaData((Map<String, Object>) gson.fromJson(documentDto.getMetaDataChunk(), Map.class));
		}
		document.setFolder(documentDto.getIsFolder());
		
		return document;
	}

    public Document documentDtoToDocument(DocumentDto documentDto, Document documentParam) throws Exception {
    	Document document = documentMapper.documentDtoToDocument(documentDto, documentParam);
    	if (!StringUtils.isEmpty(documentDto.getMetaDataChunk())) {
			document.setMetaData((Map<String, Object>) gson.fromJson(documentDto.getMetaDataChunk(), Map.class));
		}
    	document.setFolder(documentDto.getIsFolder());
    	
		return document;
    }
    
    public DocumentDto documentToDocumentDto(Document document) throws Exception {
    	DocumentDto documentDto = documentMapper.documentToDocumentDto(document);
    	if (document.getMetaData() != null) {
    		documentDto.setMetaDataChunk(gson.toJson(document.getMetaData(), Map.class));
    	}
    	documentDto.setOriginalFileName(documentDto.getFileName());
    	documentDto.setIsFolder(document.isFolder());

		return documentDto;
    }

}
