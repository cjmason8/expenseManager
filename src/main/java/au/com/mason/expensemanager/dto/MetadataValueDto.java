package au.com.mason.expensemanager.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetadataValueDto {
	private Long id;
	private String value;
	private MetadataKeyDto metadataKey;
}
