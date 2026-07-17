package au.com.mason.expensemanager.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetadataKeyWithValuesDto {
	private Long id;
	private String name;
	private List<MetadataValueDto> values = new ArrayList<>();
}
