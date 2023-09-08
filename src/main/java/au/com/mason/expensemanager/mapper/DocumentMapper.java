package au.com.mason.expensemanager.mapper;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.dto.DocumentDto;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper implements BaseMapper<Document, DocumentDto> {

    private static Gson gson = new GsonBuilder().serializeNulls().create();

    public Document dtoToEntity(DocumentDto documentDto) {
        if ( documentDto == null ) {
            return null;
        }

        Document document = new Document();

        if ( documentDto.getId() != null ) {
            document.setId( documentDto.getId() );
        }
        document.setFileName( documentDto.getFileName() );
        document.setFolderPath( documentDto.getFolderPath() );
        document.setOriginalFileName( documentDto.getOriginalFileName() );
        if (!StringUtils.isEmpty(documentDto.getMetaDataChunk())) {
            document.setMetaData((Map<String, Object>) gson.fromJson(documentDto.getMetaDataChunk(), Map.class));
        }
        document.setFolder(documentDto.getIsFolder());

        return document;
    }

    public DocumentDto entityToDto(Document document) {
        if ( document == null ) {
            return null;
        }

        DocumentDto documentDto = new DocumentDto();

        documentDto.setId( document.getId() );
        documentDto.setFileName( document.getFileName() );
        documentDto.setFolderPath( document.getFolderPath() );
        documentDto.setOriginalFileName( document.getOriginalFileName() );
        if (document.getMetaData() != null) {
            documentDto.setMetaDataChunk(gson.toJson(document.getMetaData(), Map.class));
        }
        documentDto.setOriginalFileName(documentDto.getFileName());
        documentDto.setIsFolder(document.isFolder());

        return documentDto;
    }
}
