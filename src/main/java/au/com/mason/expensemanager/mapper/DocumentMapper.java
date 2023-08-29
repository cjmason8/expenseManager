package au.com.mason.expensemanager.mapper;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.dto.DocumentDto;

public class DocumentMapper {
    public Document documentDtoToDocument(DocumentDto documentDto) {
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

        return document;
    }

    public DocumentDto documentToDocumentDto(Document document) {
        if ( document == null ) {
            return null;
        }

        DocumentDto documentDto = new DocumentDto();

        documentDto.setId( document.getId() );
        documentDto.setFileName( document.getFileName() );
        documentDto.setFolderPath( document.getFolderPath() );
        documentDto.setOriginalFileName( document.getOriginalFileName() );

        return documentDto;
    }
}
