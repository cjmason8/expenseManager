package au.com.mason.expensemanager.dto;

import au.com.mason.expensemanager.domain.RefData;
import java.util.Map;

public class RefDataDto {

	private Long id;
	private String value;
	private String type;
	private String typeDescription;
	private String description;
	private String metaDataChunk;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
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

	public String getTypeDescription() {
		return typeDescription;
	}

	public void setTypeDescription(String typeDescription) {
		this.typeDescription = typeDescription;
	}

	public String getMetaDataChunk() {
		return metaDataChunk;
	}

	public void setMetaDataChunk(String metaDataChunk) {
		this.metaDataChunk = metaDataChunk;
	}
	
}
