package au.com.mason.expensemanager.dto;

import au.com.mason.expensemanager.util.S3Keys;
import java.util.Comparator;
import java.util.UUID;

public class DocumentDto implements Comparator<DocumentDto>, Comparable<DocumentDto> {

	private UUID id;
	private String fileName;
	private String originalFileName;
	private boolean isFolder;
	private String metaDataChunk;
	private String folderPath;
	
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
		return S3Keys.normalize(folderPath);
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
	
	public String getFilePath() {
		String parent = S3Keys.normalize(folderPath);
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
		if (o.getIsFolder() == isFolder) {
			return fileName.toLowerCase().compareTo(o.getFileName().toLowerCase());
		}
		else if (o.getIsFolder()) {
			return 1;
		}
		else {
			return -1;
		}
	}

	@Override
	public int compare(DocumentDto o1, DocumentDto o2) {
		if (o1.getIsFolder() == o2.getIsFolder()) {
			return o2.getFileName().toLowerCase().compareTo(o1.getFileName().toLowerCase());
		}
		else if (o1.getIsFolder()) {
			return 1;
		}
		else {
			return -1;
		}
	}
	
}
