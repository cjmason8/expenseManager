package au.com.mason.expensemanager.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import au.com.mason.expensemanager.dao.DocumentDao;
import au.com.mason.expensemanager.domain.Document;
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

	public Document updateDocument(Document document) {
		if (document.isFolder() && document.getOriginalFileName() != null
				&& !document.getOriginalFileName().equals(document.getFileName())) {
			String oldKey = S3Keys.join(document.getFolderPath(), document.getOriginalFileName());
			String newKey = S3Keys.join(document.getFolderPath(), document.getFileName());
			s3Service.renamePrefix(oldKey, newKey);
			documentDao.updateDirectoryPaths(oldKey, newKey);
		}

		documentDao.update(document);
		return document;
	}

	public Document createDocument(String path, String type, MultipartFile file) throws Exception {
		byte[] bytes = file.getBytes();
		String parentFolderKey = S3Keys.join(S3Keys.join(docsRoot, "expenseManager"), type);
		if (path != null) {
			parentFolderKey = toBucketKey(path);
		}

		Document document = new Document();
		document.setFileName(file.getOriginalFilename());
		document.setFolderPath(parentFolderKey);
		if (type.equals("documents")) {
			setMetadata(path, document);
		}

		Document saved = documentDao.create(document);
		s3Service.putObjectWithFolders(parentFolderKey, saved.getId(), bytes, file.getContentType());
		return saved;
	}

	public Document createDocumentFromEmailForExpense(byte[] file, String fileName) throws Exception {
		String parentFolderKey = S3Keys.join(S3Keys.join(docsRoot, "expenseManager"), "expenses");

		Document document = new Document();
		document.setFileName(fileName);
		document.setFolderPath(parentFolderKey);

		Document saved = documentDao.create(document);
		s3Service.putObjectWithFolders(parentFolderKey, saved.getId(), file, "application/octet-stream");
		return saved;
	}

	public Document createDocumentForRentalStatement(byte[] file, String fileName, String folderPath,
			Map<String, Object> metaData) throws Exception {
		String parentFolderKey = S3Keys.join(S3Keys.join(docsRoot, IP_FOLDER_PATH), folderPath.replaceFirst("^/+", ""));

		Document document = new Document();
		document.setMetaData(metaData);
		document.setFileName(fileName);
		document.setFolderPath(S3Keys.normalize(parentFolderKey));

		Document saved = documentDao.create(document);
		s3Service.putObjectWithFolders(document.getFolderPath(), saved.getId(), file, "application/octet-stream");
		return saved;
	}

	private void setMetadata(String path, Document document) {
		if (path == null) {
			return;
		}
		String keyPath = toBucketKey(path);
		int lastSlash = keyPath.lastIndexOf('/');
		if (lastSlash < 0) {
			return;
		}
		String parentFolderKey = keyPath.substring(0, lastSlash);
		String parentFolderName = keyPath.substring(lastSlash + 1);

		Document parent = documentDao.getFolder(parentFolderKey, parentFolderName);
		document.setMetaData(parent.getMetaData());
	}

	public Document createDirectory(Document directory) {
		String parentKey;
		if (directory.getFolderPath().contains("root")) {
			String rel = directory.getFolderPath().replace("root", "").replaceAll("^/+", "");
			parentKey = S3Keys.join(S3Keys.join(docsRoot, "expenseManager/filofax"), rel);
		}
		else {
			parentKey = S3Keys.normalize(directory.getFolderPath());
		}

		String folderName = directory.getFileName();
		String newFolderKey = S3Keys.join(parentKey, folderName);
		s3Service.ensureFolderPrefix(newFolderKey);

		int li = parentKey.lastIndexOf('/');
		String parentFolderPath = li < 0 ? "" : parentKey.substring(0, li);
		String parentFolderName = li < 0 ? parentKey : parentKey.substring(li + 1);

		Document document = new Document();
		document.setFileName(folderName);
		document.setFolderPath(parentKey);
		setMetaData(directory, parentFolderPath, parentFolderName, document);
		document.setFolder(true);

		return documentDao.create(document);
	}

	private void setMetaData(Document directory, String parentFolderPath, String parentFolderName,
			Document document) {
		Map<String, Object> metaData = new HashMap<>();
		if (!parentFolderName.equals("filofax")) {
			Document parent = documentDao.getFolder(parentFolderPath, parentFolderName);
			metaData.putAll(parent.getMetaData());
		}
		if (directory.getMetaData() != null) {
			metaData.putAll(directory.getMetaData());
		}
		document.setMetaData(metaData);
	}

	public void deleteDocument(Document document) {
		if (document.isFolder()) {
			s3Service.deleteAllUnderPrefix(document.getFilePath());
			documentDao.deleteDirectory(S3Keys.join(document.getFolderPath(), document.getFileName()));
		}
		else {
			s3Service.deleteObject(document.getFilePath());
		}
		documentDao.deleteById(document.getId());
	}

	public Document getById(UUID id) throws Exception {
		return documentDao.getById(id);
	}

	public List<Document> getAll(String folder, boolean includeArchived) throws Exception {
		return documentDao.getAll(toBucketKey(folder), includeArchived);
	}

	public void moveFiles(String destinationParentFolderKey, UUID[] files) {
		String destParent = S3Keys.normalize(destinationParentFolderKey);
		Arrays.asList(files).forEach(fileId -> {
			Document file = documentDao.getById(fileId);
			if (file.isFolder()) {
				throw new UnsupportedOperationException("moving folders is not supported");
			}
			String destKey = S3Keys.join(destParent, file.getId().toString());
			s3Service.moveObject(file.getFilePath(), destKey);
			file.setFolderPath(destParent);
			documentDao.update(file);
		});
	}

	/**
	 * Moves a file object’s S3 key to {@code newParentFolderKey}/{@code document.getId()}} and updates the entity.
	 */
	public void moveDocumentToParentFolder(Document document, String newParentFolderKey) {
		String destParent = S3Keys.normalize(newParentFolderKey);
		String destKey = S3Keys.join(destParent, document.getId().toString());
		s3Service.moveObject(document.getFilePath(), destKey);
		document.setFolderPath(destParent);
		documentDao.update(document);
	}

	/** Maps UI paths ({@code /docs/...}) and raw keys to a normalized key under the bucket. */
	public String toBucketKey(String uiPathOrKey) {
		if (uiPathOrKey == null) {
			return null;
		}
		String p = uiPathOrKey.replace("/docs", docsRoot).replace("\\", "/");
		return S3Keys.normalize(p);
	}
}
