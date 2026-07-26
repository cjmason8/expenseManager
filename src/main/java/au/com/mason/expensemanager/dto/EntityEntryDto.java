package au.com.mason.expensemanager.dto;

public class EntityEntryDto {

	private Long id;
	private String name;
	private String description;
	private String type;
	private String link;
	private DocumentDto documentDto;
	private String metaDataChunk;
	private String dataChunk;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public DocumentDto getDocumentDto() {
		return documentDto;
	}

	public void setDocumentDto(DocumentDto documentDto) {
		this.documentDto = documentDto;
	}

	public String getMetaDataChunk() {
		return metaDataChunk;
	}

	public void setMetaDataChunk(String metaDataChunk) {
		this.metaDataChunk = metaDataChunk;
	}

	public String getDataChunk() {
		return dataChunk;
	}

	public void setDataChunk(String dataChunk) {
		this.dataChunk = dataChunk;
	}

}
