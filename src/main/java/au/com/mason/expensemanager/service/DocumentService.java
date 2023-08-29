package au.com.mason.expensemanager.service;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.repository.DocumentRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class DocumentService extends BaseService<Document> {

	@Value("${docs.location}")
	private String docsFolder;
	
	public static final String IP_FOLDER_PATH = "/expenseManager/filofax/IPs";
	
	@Autowired
	private DocumentRepository documentRepository;

	protected DocumentService() {
		super(Document.class);
	}

	public Document updateDocument(Document document) {
		documentRepository.save(document);
		
		if (document.isFolder() && !document.getOriginalFileName().equals(document.getFileName())) {
			documentRepository.updateDirectoryPaths(document.getFolderPath() + "/" + document.getOriginalFileName(), document.getFolderPath() + "/" + document.getFileName());
		}
		
		return document;
	}
	
	public Document createDocument(String path, String type, MultipartFile file) throws Exception {
		byte[] bytes = file.getBytes();
		String folderPathString = docsFolder + "/expenseManager/" + type;
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
		
		return documentRepository.save(document);
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

		return documentRepository.save(document);
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

		return documentRepository.save(document);
	}

	private void setMetadata(String path, Document document) {
		String parentFolderPath = path.substring(0, path.lastIndexOf("/"));
		String parentFolderName = path.substring(path.lastIndexOf("/") + 1);
		
		Document parent = documentRepository.getFolder(parentFolderPath, parentFolderName);
		document.setMetaData(parent.getMetaData());
	}
	
	public Document createDirectory(Document directory) throws Exception {
		String folderPathString = "";
		if (directory.getFolderPath().indexOf("root") != -1) {
			folderPathString = docsFolder + "/expenseManager/filofax/" + directory.getFolderPath().replace("root", "") + "/";
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
		
		return documentRepository.save(document);
	}

	private void setMetaData(Document directory, String parentFolderPath, String parentFolderName,
			Document document) {
		Map<String, Object> metaData = new HashMap<>();
		if (!parentFolderName.equals("filofax")) {
			Document parent = documentRepository.getFolder(parentFolderPath, parentFolderName);
			metaData.putAll(parent.getMetaData());
		}
		if (directory.getMetaData() != null) {
			metaData.putAll(directory.getMetaData());
		}
		document.setMetaData(metaData);
	}
	
	public void deleteDocument(Document document) {
		if (document.isFolder()) {
			documentRepository.deleteDirectory(document.getFolderPath() + "/" + document.getFileName());
		}
		documentRepository.deleteById(document.getId());
	}
	
	public Document getById(Long id) {
		return findById(documentRepository, id);
	}
	
	public List<Document> getAll(String folder) throws Exception {
		return documentRepository.findByFolderPath(folder);
	}
	
	public void moveFiles(String fullFolderPath, Long[] files) {
		Arrays.asList(files).forEach(fileId -> {
			Document file = getById(fileId);
			if (file == null) {
				throw new RuntimeException(String.format("File %s could not be found.", fileId));
			}
			
			try {
				Files.move(Paths.get(file.getFolderPath() + "/" + file.getFileName()),
						Paths.get(fullFolderPath + "/" + file.getFileName()));
			}
			catch (IOException e) {
				throw new RuntimeException("error moving file", e);
			}
			
			file.setFolderPath(fullFolderPath);
			documentRepository.save(file);
		});
		
	}
	
}
