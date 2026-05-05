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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import au.com.mason.expensemanager.dao.DocumentDao;
import au.com.mason.expensemanager.domain.Document;

@Component
public class DocumentService {

	@Value("${docs.location}")
	private String docsFolder;
	
	public static final String IP_FOLDER_PATH = "/expenseManager/filofax/IPs";
	
	@Autowired
	private DocumentDao documentDao;
	
	public Document updateDocument(Document document) {
		document.setFolderPath(normalizeDocsPath(document.getFolderPath()));
		documentDao.update(document);
		
		if (document.isFolder() && document.getOriginalFileName() != null && !document.getOriginalFileName().equals(document.getFileName())) {
			documentDao.updateDirectoryPaths(document.getFolderPath() + "/" + document.getOriginalFileName(), document.getFolderPath() + "/" + document.getFileName());
		}
		
		return document;
	}
	
	public Document createDocument(String path, String type, MultipartFile file) throws Exception {
		byte[] bytes = file.getBytes();
		String folderPathString = resolveFolderPath(path, type);
		String filePathString = folderPathString + "/" + file.getOriginalFilename();
		Path folderPath = Paths.get(folderPathString);
		Path filePath = Paths.get(filePathString);
		if (!Files.exists(folderPath)) {
			Files.createDirectories(folderPath);
		}
		Files.write(filePath, bytes);
		
		Document document = new Document();
		document.setFileName(file.getOriginalFilename());
		document.setFolderPath(folderPathString);
		if (type.equals("documents")) {
			setMetadata(folderPathString, document);
		}
		
		return documentDao.create(document);
	}

	private String resolveFolderPath(String path, String type) {
		String defaultPath = docsFolder + "/expenseManager/" + type;
		if (path == null || path.isBlank()) {
			return defaultPath;
		}
		return normalizeDocsPath(path);
	}

	public String normalizeDocsPath(String path) {
		if (path == null || path.isBlank()) {
			return path;
		}
		if (path.equals("/docs")) {
			return docsFolder;
		}
		if (path.startsWith("/docs/")) {
			return docsFolder + path.substring("/docs".length());
		}
		return path;
	}
	
	public Document createDocumentFromEmailForExpense(byte[] file, String fileName) throws Exception {
		String folderPathString = docsFolder + "/expenseManager/expenses";
		String filePathString = folderPathString + "/" + fileName;
		Path folderPath = Paths.get(folderPathString);
		Path filePath = Paths.get(filePathString);
		if (!Files.exists(folderPath)) {
			Files.createDirectory(folderPath);
		}
		Files.write(filePath, file);
		
		Document document = new Document();
		document.setFileName(fileName);
		document.setFolderPath(folderPathString);
		
		return documentDao.create(document);
	}
	
	public Document createDocumentForRentalStatement(byte[] file, String fileName, String folderPath, Map<String, Object> metaData) throws Exception {
		String folderPathString = docsFolder + IP_FOLDER_PATH + folderPath;
		String filePathString = folderPathString + "/" + fileName;
		Path reqFolderPath = Paths.get(folderPathString);
		Path filePath = Paths.get(filePathString);
		if (!Files.exists(reqFolderPath)) {
			Files.createDirectory(reqFolderPath);
		}
		Files.write(filePath, file);
		
		Document document = new Document();
		document.setMetaData(metaData);
		document.setFileName(fileName);
		document.setFolderPath(folderPathString);
		
		return documentDao.create(document);
	}

	private void setMetadata(String path, Document document) {
		String parentFolderPath = path.substring(0, path.lastIndexOf("/"));
		String parentFolderName = path.substring(path.lastIndexOf("/") + 1);

		try {
			Document parent = documentDao.getFolder(parentFolderPath, parentFolderName);
			document.setMetaData(parent.getMetaData());
		}
		catch (EmptyResultDataAccessException e) {
			document.setMetaData(new HashMap<>());
		}
	}
	
	public Document createDirectory(Document directory) {
		String folderPathString = "";
		if (directory.getFolderPath().contains("root")) {
			folderPathString = docsFolder + "/expenseManager/filofax/" + directory.getFolderPath().replace("root", "") + "/";
		} else {
			folderPathString = normalizeDocsPath(directory.getFolderPath());
		}

		File folder = new File(folderPathString + "/" + directory.getFileName());
		folder.mkdir();
		
		String parentFolderPath = folder.getParent().substring(0, folder.getParent().lastIndexOf("/"));
		String parentFolderName = folder.getParent().substring(folder.getParent().lastIndexOf("/") + 1);

		Document document = new Document();
		document.setFileName(folder.getName());
		document.setFolderPath(folder.getParent());
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
			documentDao.deleteDirectory(document.getFolderPath() + "/" + document.getFileName());
		}
		documentDao.deleteById(document.getId());
	}
	
	public Document getById(Long id) throws Exception {
		return documentDao.getById(id);
	}
	
	public List<Document> getAll(String folder, boolean includeArchived) throws Exception {
		Map<Long, Document> uniqueDocuments = new LinkedHashMap<>();
		for (String candidateFolderPath : getFolderPathCandidates(folder)) {
			List<Document> documents = documentDao.getAll(candidateFolderPath, includeArchived);
			documents.forEach(doc -> uniqueDocuments.put(doc.getId(), doc));
		}
		return new ArrayList<>(uniqueDocuments.values());
	}

	private Set<String> getFolderPathCandidates(String folder) {
		Set<String> candidates = new LinkedHashSet<>();
		if (folder == null || folder.isBlank()) {
			return candidates;
		}
		addFolderPathVariants(candidates, folder);
		addFolderPathVariants(candidates, normalizeDocsPath(folder));
		return candidates;
	}

	private void addFolderPathVariants(Set<String> candidates, String path) {
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
	
	public void moveFiles(String fullFolderPath, Long[] files) {
		String normalizedFolderPath = normalizeDocsPath(fullFolderPath);
		Arrays.asList(files).forEach(fileId -> {
			Document file = documentDao.getById(fileId);
			
			try {
				Files.move(Paths.get(file.getFolderPath() + "/" + file.getFileName()),
						Paths.get(normalizedFolderPath + "/" + file.getFileName()));
			}
			catch (IOException e) {
				throw new RuntimeException("error moving file", e);
			}
			
			file.setFolderPath(normalizedFolderPath);
			documentDao.update(file);
		});
		
	}
	
}
