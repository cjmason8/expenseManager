package au.com.mason.expensemanager.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationDto {
	private Long id;
	private ExpenseDto expense;
	private String message;
	private String createdDateString;
	private boolean read;
	private boolean removed;
}
