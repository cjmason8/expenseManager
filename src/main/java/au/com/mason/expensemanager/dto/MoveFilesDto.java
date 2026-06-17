package au.com.mason.expensemanager.dto;

import java.util.UUID;

public class MoveFilesDto {

	private String directoryTo;
	private UUID[] fileIds;

	public String getDirectoryTo() {
		return directoryTo;
	}

	public void setDirectoryTo(String directoryTo) {
		this.directoryTo = directoryTo;
	}

	public UUID[] getFileIds() {
		return fileIds;
	}

	public void setFileIds(UUID[] fileIds) {
		this.fileIds = fileIds;
	}

}
