package au.com.mason.expensemanager.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import au.com.mason.expensemanager.dao.DocumentDao;
import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.dto.DocumentDto;
import au.com.mason.expensemanager.mapper.DocumentMapperWrapper;

@Component
public class DocumentService {
	
	public static final String IP_FOLDER_PATH = "/docs/expenseManager/filofax/IPs";
	
	private Gson gson = new GsonBuilder().serializeNulls().create();
	
	@Autowired
	private DocumentMapperWrapper documentMapperWrapper;
	
	@Autowired
	private DocumentDao documentDao;
	
	public DocumentDto updateDocument(DocumentDto documentDto) throws Exception {
		Document updatedDocument = documentDao.getById(documentDto.getId());
		updatedDocument = documentMapperWrapper.documentDtoToDocument(documentDto, updatedDocument);
		documentDao.update(updatedDocument);
		
		if (updatedDocument.isFolder() && !documentDto.getOriginalFileName().equals(documentDto.getFileName())) {
			documentDao.updateDirectoryPaths(documentDto.getFolderPath() + "/" + documentDto.getOriginalFileName(), documentDto.getFolderPath() + "/" + documentDto.getFileName());
		}
		
		return documentMapperWrapper.documentToDocumentDto(updatedDocument);
	}
	
	public DocumentDto createDocument(String path, String type, MultipartFile file) throws Exception {
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
		
		return documentMapperWrapper.documentToDocumentDto(documentDao.create(document));
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
	
	public DocumentDto createDirectory(DocumentDto directoryDto) throws Exception {
		String folderPathString = "";
		if (directoryDto.getFolderPath().indexOf("root") != -1) {
			folderPathString = "/docs/expenseManager/filofax/" + directoryDto.getFolderPath().replace("root", "") + "/";
		} else {
			folderPathString = directoryDto.getFolderPath();
		}

		File folder = new File(folderPathString + "/" + directoryDto.getFileName());
		folder.mkdir();
		
		String parentFolderPath = folder.getParent().substring(0, folder.getParent().lastIndexOf("/"));
		String parentFolderName = folder.getParent().substring(folder.getParent().lastIndexOf("/") + 1);

		Document document = new Document();
		document.setFileName(folder.getName());
		document.setFolderPath(folder.getParent());
		setMetaData(directoryDto, parentFolderPath, parentFolderName, document);
		document.setFolder(true);
		
		return documentMapperWrapper.documentToDocumentDto(documentDao.create(document));
	}

	private void setMetaData(DocumentDto directoryDto, String parentFolderPath, String parentFolderName,
			Document document) {
		Map<String, Object> metaData = new HashMap<>();
		if (directoryDto.getMetaDataChunk() != null) {
			metaData.putAll(gson.fromJson(directoryDto.getMetaDataChunk(), Map.class));
		}
		if (!parentFolderName.equals("filofax")) {
			Document parent = documentDao.getFolder(parentFolderPath, parentFolderName);
			metaData.putAll(parent.getMetaData());
		}
		document.setMetaData(metaData);
	}
	
	public void deleteDocument(DocumentDto documentDto) {
		if (documentDto.getIsFolder()) {
			documentDao.deleteDirectory(documentDto.getFolderPath() + "/" + documentDto.getFileName());
		}
		documentDao.deleteById(documentDto.getId());
	}
	
	public DocumentDto getById(Long id) throws Exception {
		Document document = documentDao.getById(id);
		
		return documentMapperWrapper.documentToDocumentDto(document);
	}
	
	public List<DocumentDto> getAll(String folder) throws Exception {
		List<DocumentDto> documentDtos = new ArrayList<>();
		for(Document document : documentDao.getAll(folder)) {
			documentDtos.add(documentMapperWrapper.documentToDocumentDto(document));
		};
		
		return documentDtos;
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
