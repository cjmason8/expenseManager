package au.com.mason.expensemanager.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import au.com.mason.expensemanager.dao.DocumentDao;
import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.EntityMetadataType;
import au.com.mason.expensemanager.util.S3Keys;

@Component
public class DocumentService {

	/**
	 * Root S3 key prefix for all documents (within the configured bucket).
	 */
	@Value("${docs.location}")
	private String docsRoot;

	public static final String IP_FOLDER_PATH = "expenseManager/filofax/IPs";

	@PostConstruct
	void normalizeDocsRoot() {
		docsRoot = S3Keys.normalize(docsRoot);
	}

	@Autowired
	private DocumentDao documentDao;

	@Autowired
	private S3Service s3Service;

	@Autowired
	private EntityMetadataService entityMetadataService;

	public Document updateDocument(Document document) {
		if (document.isFolder() && document.getOriginalFileName() != null
			&& !document.getOriginalFileName().equals(document.getFileName())) {
			String parentPrefix = S3Keys.toBucketPrefix(document.getFolderPath());
			String oldKey = S3Keys.join(parentPrefix, document.getOriginalFileName());
			String newKey = S3Keys.join(parentPrefix, document.getFileName());
			s3Service.renamePrefix(oldKey, newKey);
			documentDao.updateDirectoryPaths(oldKey, newKey);
		}

		documentDao.update(document);
		persistMetadata(document);
		hydrateDocument(document);
		return document;
	}

	public Document createDocument(String path, String type, MultipartFile file) throws Exception {
		byte[] bytes = file.getBytes();
		String uploadType = normalizeUploadType(type);
		String parentFolderPath = resolveFolderPath(path, uploadType);

		Document document = new Document();
		String originalName = file.getOriginalFilename();
		document.setFileName(originalName);
		document.setOriginalFileName(originalName);
		document.setFolderPath(parentFolderPath);
		if (uploadType.equals("documents")) {
			setMetadata(path, document);
		}

		Document saved = documentDao.create(document);
		persistMetadata(saved);
		s3Service.putObjectWithFolders(S3Keys.toBucketPrefix(parentFolderPath), saved.getId(), bytes,
			file.getContentType());
		saved.setOriginalFileName(originalName);
		hydrateDocument(saved);
		return saved;
	}

	private static String normalizeUploadType(String type) {
		if (type == null || type.isBlank() || "undefined".equalsIgnoreCase(type) || "null".equalsIgnoreCase(type)) {
			throw new IllegalArgumentException(
				"Document upload requires a type (expenses, incomes, donations, or documents)");
		}
		return type.trim();
	}

	public Document createDocumentFromEmailForExpense(byte[] file, String fileName) throws Exception {
		String parentFolderPath = "/docs/expenseManager/expenses";

		Document document = new Document();
		document.setFileName(fileName);
		document.setFolderPath(parentFolderPath);

		Document saved = documentDao.create(document);
		s3Service.putObjectWithFolders(S3Keys.toBucketPrefix(parentFolderPath), saved.getId(), file,
			"application/octet-stream");
		return saved;
	}

	public Document createDocumentForRentalStatement(byte[] file, String fileName, String folderPath,
		Map<String, Object> metaData) throws Exception {
		String parentFolderPath = S3Keys
			.toUiFolderPath("/docs/expenseManager/filofax/IPs/" + folderPath.replaceFirst("^/+", ""));

		Document document = new Document();
		document.setMetaData(metaData);
		document.setFileName(fileName);
		document.setFolderPath(parentFolderPath);

		Document saved = documentDao.create(document);
		persistMetadata(saved);
		s3Service.putObjectWithFolders(S3Keys.toBucketPrefix(parentFolderPath), saved.getId(), file,
			"application/octet-stream");
		hydrateDocument(saved);
		return saved;
	}

	private String resolveFolderPath(String path, String type) {
		String defaultPath = "/docs/expenseManager/" + type;
		if (path == null || path.isBlank()) {
			return defaultPath;
		}
		return S3Keys.toUiFolderPath(path);
	}

	private void setMetadata(String path, Document document) {
		if (path == null) {
			return;
		}
		String uiPath = S3Keys.toUiFolderPath(path);
		int lastSlash = uiPath.lastIndexOf('/');
		if (lastSlash < 0) {
			return;
		}
		String parentFolderPath = uiPath.substring(0, lastSlash);
		String parentFolderName = uiPath.substring(lastSlash + 1);

		Document parent = documentDao.getFolder(parentFolderPath, parentFolderName);
		if (parent == null) {
			return;
		}
		hydrateDocument(parent);
		document.setMetaData(parent.getMetaData());
	}

	public Document createDirectory(Document directory) {
		String parentPath;
		if (directory.getFolderPath().contains("root")) {
			String rel = directory.getFolderPath().replace("root", "").replaceAll("^/+", "");
			parentPath = S3Keys.toUiFolderPath("/docs/expenseManager/filofax/" + rel);
		} else {
			parentPath = S3Keys.toUiFolderPath(directory.getFolderPath());
		}

		String folderName = directory.getFileName();
		String newFolderKey = S3Keys.join(S3Keys.toBucketPrefix(parentPath), folderName);
		s3Service.ensureFolderPrefix(newFolderKey);

		int li = parentPath.lastIndexOf('/');
		String parentFolderPath = li < 0 ? "" : parentPath.substring(0, li);
		String parentFolderName = li < 0 ? parentPath : parentPath.substring(li + 1);

		Document document = new Document();
		document.setFileName(folderName);
		document.setFolderPath(parentPath);
		setMetaData(directory, parentFolderPath, parentFolderName, document);
		document.setFolder(true);

		Document saved = documentDao.create(document);
		persistMetadata(saved);
		hydrateDocument(saved);
		return saved;
	}

	private void setMetaData(Document directory, String parentFolderPath, String parentFolderName, Document document) {
		Map<String, Object> metaData = new HashMap<>();
		if (!parentFolderName.equals("filofax")) {
			Document parent = documentDao.getFolder(parentFolderPath, parentFolderName);
			if (parent != null) {
				hydrateDocument(parent);
				if (parent.getMetaData() != null) {
					metaData.putAll(parent.getMetaData());
				}
			}
		}
		if (directory.getMetaData() != null) {
			metaData.putAll(directory.getMetaData());
		}
		document.setMetaData(metaData);
	}

	public void deleteDocument(Document document) {
		if (document.isFolder()) {
			s3Service.deleteAllUnderPrefix(document.getFilePath());
			documentDao
				.deleteDirectory(S3Keys.join(S3Keys.toBucketPrefix(document.getFolderPath()), document.getFileName()));
		} else {
			s3Service.deleteObject(document.getFilePath());
		}
		if (document.getId() != null) {
			entityMetadataService.deleteForEntity(EntityMetadataType.DOCUMENT, document.getId().toString());
		}
		documentDao.deleteById(document.getId());
	}

	public Document getById(UUID id) throws Exception {
		Document document = documentDao.getById(id);
		hydrateDocument(document);
		return document;
	}

	public List<Document> getAll(String folder, boolean includeArchived) throws Exception {
		Map<UUID, Document> uniqueDocuments = new LinkedHashMap<>();
		for (String candidateFolderPath : getFolderPathCandidates(folder)) {
			List<Document> documents = documentDao.getAll(candidateFolderPath, includeArchived);
			documents.forEach(doc -> uniqueDocuments.put(doc.getId(), doc));
		}
		List<Document> results = new ArrayList<>(uniqueDocuments.values());
		hydrateDocuments(results);
		return results;
	}

	public void hydrateDocument(Document document) {
		if (document == null || document.getId() == null) {
			return;
		}
		hydrateDocuments(List.of(document));
	}

	public void hydrateDocuments(List<Document> documents) {
		entityMetadataService.hydrateList(EntityMetadataType.DOCUMENT, documents,
			document -> document.getId() == null ? null : document.getId().toString(),
			(entity, entityMetadata, objectMap, stringMap) -> {
				entity.setEntityMetadata(entityMetadata);
				entity.setMetaData(objectMap);
			});
	}

	private void persistMetadata(Document document) {
		if (document == null || document.getId() == null) {
			return;
		}
		entityMetadataService.replace(EntityMetadataType.DOCUMENT, document.getId().toString(), document.getMetaData());
	}

	private Set<String> getFolderPathCandidates(String folder) {
		Set<String> candidates = new LinkedHashSet<>();
		if (folder == null || folder.isBlank()) {
			return candidates;
		}
		addFolderPathVariants(candidates, folder);
		addFolderPathVariants(candidates, S3Keys.toUiFolderPath(folder));
		addFolderPathVariants(candidates, S3Keys.toBucketPrefix(folder));
		return candidates;
	}

	private static void addFolderPathVariants(Set<String> candidates, String path) {
		if (path == null || path.isBlank()) {
			return;
		}
		candidates.add(path);
		if (path.endsWith("/")) {
			candidates.add(path.substring(0, path.length() - 1));
		} else {
			candidates.add(path + "/");
		}
	}

	public void moveFiles(String destinationParentFolderKey, UUID[] files) {
		String destParent = S3Keys.toUiFolderPath(destinationParentFolderKey);
		Arrays.asList(files).forEach(fileId -> {
			Document file = documentDao.getById(fileId);
			if (file.isFolder()) {
				moveFolder(file, destParent);
			} else {
				String destKey = S3Keys.join(S3Keys.toBucketPrefix(destParent), file.getId().toString());
				s3Service.moveObject(resolveSourceKey(file), destKey);
				file.setFolderPath(destParent);
				documentDao.update(file);
			}
		});
	}

	/**
	 * Files are normally stored at {@code folderPath/id}, but objects migrated
	 * from the old filesystem layout can still live at {@code folderPath/fileName}.
	 */
	private String resolveSourceKey(Document file) {
		String idKey = file.getFilePath();
		if (s3Service.objectExists(idKey)) {
			return idKey;
		}
		String legacyKey = S3Keys.join(S3Keys.toBucketPrefix(file.getFolderPath()), file.getFileName());
		if (s3Service.objectExists(legacyKey)) {
			return legacyKey;
		}
		throw new IllegalStateException(
			"S3 object not found for document " + file.getId() + " (tried " + idKey + " and " + legacyKey + ")");
	}

	private void moveFolder(Document folder, String destParent) {
		String oldPrefix = S3Keys.join(S3Keys.toBucketPrefix(folder.getFolderPath()), folder.getFileName());
		String destParentPrefix = S3Keys.toBucketPrefix(destParent);
		String newPrefix = S3Keys.join(destParentPrefix, folder.getFileName());
		if (oldPrefix.equals(newPrefix)) {
			return;
		}
		if (destParentPrefix.equals(oldPrefix) || destParentPrefix.startsWith(oldPrefix + "/")) {
			throw new IllegalArgumentException("cannot move a folder into itself or one of its subfolders");
		}
		s3Service.renamePrefix(oldPrefix, newPrefix);
		documentDao.updateDirectoryPaths(oldPrefix, newPrefix);
		folder.setFolderPath(destParent);
		documentDao.update(folder);
	}

	/**
	 * Moves a file object’s S3 key to
	 * {@code newParentFolderKey}/{@code document.getId()}} and updates the entity.
	 */
	public void moveDocumentToParentFolder(Document document, String newParentFolderKey) {
		String destParent = S3Keys.toUiFolderPath(newParentFolderKey);
		String destKey = S3Keys.join(S3Keys.toBucketPrefix(destParent), document.getId().toString());
		s3Service.moveObject(resolveSourceKey(document), destKey);
		document.setFolderPath(destParent);
		documentDao.update(document);
	}

	/** Maps UI / DB folder paths to an S3 key prefix within the bucket. */
	public String toBucketKey(String uiPathOrKey) {
		return S3Keys.toBucketPrefix(uiPathOrKey);
	}
}
