package au.com.mason.expensemanager.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmailTrawlerResponseDto {

	private boolean success;
	private String summary;
	private List<String> details = new ArrayList<>();
	private String error;

	public EmailTrawlerResponseDto(boolean success, String summary, List<String> details, String error) {
		this.success = success;
		this.summary = summary;
		this.details = details == null ? new ArrayList<>() : details;
		this.error = error;
	}

}
