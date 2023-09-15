package au.com.mason.expensemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class DocumentListDto {
	private String folderPath;
	private Boolean includeArchived;
}
