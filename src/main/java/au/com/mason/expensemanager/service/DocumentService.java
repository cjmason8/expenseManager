package au.com.mason.expensemanager.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import au.com.mason.expensemanager.dao.DocumentDao;
import au.com.mason.expensemanager.domain.Document;

@Component
public class DocumentService {
	
	public static final String IP_FOLDER_PATH = "/docs/expenseManager/filofax/IPs";
	
	@Autowired
	private DocumentDao documentDao;
	
	public Document updateDocument(Document document) throws Exception {
		documentDao.update(document);
		
		if (document.isFolder() && !document.getOriginalFileName().equals(document.getFileName())) {
			documentDao.updateDirectoryPaths(document.getFolderPath() + "/" + document.getOriginalFileName(), document.getFolderPath() + "/" + document.getFileName());
		}
		
		return document;
	}
	
	public Document createDocument(String path, String type, MultipartFile file) throws Exception {
		byte[] bytes = file.getBytes();
		String folderPathString = "/docs/expenseManager/" + type;
		if (path != null) {
			folderPathString = path;
		}
		String filePathString = folderPathString + "/" + file.getOriginalFilename();
		Path folderPath = Paths.get(folderPathString);
		Path filePath = Paths.get(filePathString);
		if (!Files.exists(folderPath)) {
			Files.createDirectory(folderPath);
		}
		Files.write(filePath, bytes);
		
		Document document = new Document();
		document.setFileName(file.getOriginalFilename());
		document.setFolderPath(folderPathString);
		if (type.equals("documents")) {
			setMetadata(path, document);
		}
		
		return document;
	}
	
	public Document createDocumentFromEmailForExpense(byte[] file, String fileName) throws Exception {
		String folderPathString = "/docs/expenseManager/expenses";
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
		String folderPathString = IP_FOLDER_PATH + folderPath;
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
		
		Document parent = documentDao.getFolder(parentFolderPath, parentFolderName);
		document.setMetaData(parent.getMetaData());
	}
	
	public Document createDirectory(Document directory) throws Exception {
		String folderPathString = "";
		if (directory.getFolderPath().indexOf("root") != -1) {
			folderPathString = "/docs/expenseManager/filofax/" + directory.getFolderPath().replace("root", "") + "/";
		} else {
			folderPathString = directory.getFolderPath();
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
		
		return document;
	}

	private void setMetaData(Document directory, String parentFolderPath, String parentFolderName,
			Document document) {
		Map<String, Object> metaData = new HashMap<>();
		if (!parentFolderName.equals("filofax")) {
			Document parent = documentDao.getFolder(parentFolderPath, parentFolderName);
			metaData.putAll(parent.getMetaData());
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
	
	public List<Document> getAll(String folder) throws Exception {
		return documentDao.getAll(folder);
	}
	
	public void moveFiles(String fullFolderPath, Long[] files) {
		Arrays.asList(files).forEach(fileId -> {
			Document file = documentDao.getById(fileId);
			
			try {
				Files.move(Paths.get(file.getFolderPath() + "/" + file.getFileName()),
						Paths.get(fullFolderPath + "/" + file.getFileName()));
			}
			catch (IOException e) {
				throw new RuntimeException("error moving file", e);
			}
			
			file.setFolderPath(fullFolderPath);
			documentDao.update(file);
		});
		
	}
	
}
