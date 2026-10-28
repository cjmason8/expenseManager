package au.com.mason.expensemanager.dto;

import java.util.UUID;

public record MaintenanceTotalBackfillDto(UUID id, String folderPath, String fileName, String total, Status status,
	String error) {

	public enum Status {
		SET, WOULD_SET, SKIPPED_EXISTING, FAILED
	}

}
