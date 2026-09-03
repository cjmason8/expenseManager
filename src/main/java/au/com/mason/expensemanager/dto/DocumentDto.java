package au.com.mason.expensemanager.dto;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

import au.com.mason.expensemanager.util.DocumentFileNameDates;
import au.com.mason.expensemanager.util.S3Keys;

public class DocumentDto implements Comparator<DocumentDto>, Comparable<DocumentDto> {

	private UUID id;
	private String fileName;
	private String originalFileName;
	private boolean isFolder;
	private String metaDataChunk;
	private String folderPath;
	private boolean isArchived;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean getIsFolder() {
		return isFolder;
	}

	public void setIsFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}

	public String getFolderPath() {
		return S3Keys.toUiFolderPath(folderPath);
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public String getMetaDataChunk() {
		return metaDataChunk;
	}

	public void setMetaDataChunk(String metaDataChunk) {
		this.metaDataChunk = metaDataChunk;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}

	public boolean getIsArchived() {
		return isArchived;
	}

	public void setIsArchived(boolean isArchived) {
		this.isArchived = isArchived;
	}

	public String getFilePath() {
		String parent = S3Keys.toBucketPrefix(folderPath);
		if (parent == null) {
			return null;
		}
		if (isFolder) {
			return S3Keys.join(parent, fileName);
		}
		if (id != null) {
			return S3Keys.join(parent, id.toString());
		}
		return S3Keys.join(parent, fileName);
	}

	@Override
	public int compareTo(DocumentDto o) {
		return compareDocuments(this, o);
	}

	@Override
	public int compare(DocumentDto o1, DocumentDto o2) {
		return compareDocuments(o1, o2);
	}

	static int compareDocuments(DocumentDto left, DocumentDto right) {
		if (left.getIsFolder() != right.getIsFolder()) {
			return left.getIsFolder() ? -1 : 1;
		}
		if (left.getIsFolder()) {
			return left.getFileName().compareToIgnoreCase(right.getFileName());
		}

		Optional<LocalDate> leftDate = DocumentFileNameDates.extractDate(left.getFileName());
		Optional<LocalDate> rightDate = DocumentFileNameDates.extractDate(right.getFileName());

		if (leftDate.isPresent() && rightDate.isPresent()) {
			int byDate = rightDate.get().compareTo(leftDate.get());
			if (byDate != 0) {
				return byDate;
			}
		}

		return left.getFileName().compareToIgnoreCase(right.getFileName());
	}

}
