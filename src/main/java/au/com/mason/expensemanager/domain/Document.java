package au.com.mason.expensemanager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Map;
import java.util.UUID;
import au.com.mason.expensemanager.hibernate.DocumentUuidJdbcType;
import au.com.mason.expensemanager.util.S3Keys;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@NamedQueries(
		value = {
				@NamedQuery(
						name = Document.GET_ALL_BY_FOLDER_PATH,
						query = "FROM Document WHERE folderPath = :folderPath AND isArchived = false"),
				@NamedQuery(
						name = Document.GET_ALL_BY_FOLDER_PATH_INCLUDE_ARCHIVED,
						query = "FROM Document WHERE folderPath = :folderPath"),
				@NamedQuery(
						name = Document.GET_ALL_BY_FOLDER_PATH_AND_FILENAME,
						query = "FROM Document WHERE folderPath = :folderPath AND fileName = :fileName"),
		})
@Entity
@Table(name="documents")
public class Document implements Metadata {

	public static final String GET_ALL_BY_FOLDER_PATH = "Document.Repository.GetAllByFolderPath";
	public static final String GET_ALL_BY_FOLDER_PATH_INCLUDE_ARCHIVED = "Document.Repository.GetAllByFolderPathIncludeArchived";
	public static final String GET_ALL_BY_FOLDER_PATH_AND_FILENAME = "Document.Repository.GetAllByFolderPathAndFilename";
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@JdbcType(DocumentUuidJdbcType.class)
	private UUID id;

	private String fileName;
	private String folderPath;
	private boolean isFolder;
	private boolean isArchived;
	
    @Column
	@JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metaData;
    
    @Transient
    private String originalFileName;

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

	public Map<String, Object> getMetaData() {
		return metaData;
	}

	public void setMetaData(Map<String, Object> metaData) {
		this.metaData = metaData;
	}

	public boolean isFolder() {
		return isFolder;
	}

	public void setFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}

	public boolean isArchived() {
		return isArchived;
	}

	public void setArchived(boolean isArchived) {
		this.isArchived = isArchived;
	}

	/**
	 * Parent folder key within the bucket (S3 prefix of this document, no trailing slash).
	 */
	public String getFolderPath() {
		return S3Keys.normalize(folderPath);
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}
	
	/**
	 * Full S3 object key: for files {@code folderPath + "/" + id}; for folders {@code folderPath + "/" + fileName}.
	 */
	@Transient
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

}
