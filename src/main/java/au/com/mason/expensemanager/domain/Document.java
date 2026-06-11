package au.com.mason.expensemanager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Getter
@Setter
@NoArgsConstructor
public class Document implements Metadata {

	public static final String GET_ALL_BY_FOLDER_PATH = "Document.Repository.GetAllByFolderPath";
	public static final String GET_ALL_BY_FOLDER_PATH_INCLUDE_ARCHIVED = "Document.Repository.GetAllByFolderPathIncludeArchived";
	public static final String GET_ALL_BY_FOLDER_PATH_AND_FILENAME = "Document.Repository.GetAllByFolderPathAndFilename";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "documents_seq")
	@SequenceGenerator(name = "documents_seq", sequenceName = "documents_seq", allocationSize = 1)
	private long id;

	private String fileName;
	private String folderPath;
	private boolean isFolder;
	private boolean isArchived;

	@Column
	@JdbcTypeCode(SqlTypes.JSON)
	private Map<String, Object> metaData;

	@Transient
	private String originalFileName;

	@Transient
	public String getFilePath() {
		return folderPath + "/" + fileName;
	}

}
