package au.com.mason.expensemanager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import au.com.mason.expensemanager.dto.DirectoryDto;
import au.com.mason.expensemanager.dto.DocumentDto;
import au.com.mason.expensemanager.dto.DonationDto;
import au.com.mason.expensemanager.dto.ExpenseDto;
import au.com.mason.expensemanager.dto.IncomeDto;
import au.com.mason.expensemanager.dto.MoveFilesDto;
import au.com.mason.expensemanager.service.DocumentService;
import au.com.mason.expensemanager.service.DonationService;
import au.com.mason.expensemanager.service.ExpenseService;
import au.com.mason.expensemanager.service.IncomeService;

@RestController
public class DocumentController {

	@Autowired
	private DonationService donationService;

	@Autowired
	private DocumentService documentService;

	@Autowired
	private ExpenseService expenseService;

	@Autowired
	private IncomeService incomeService;
	
	private static Logger LOGGER = LogManager.getLogger(DocumentController.class);
	
	@PostMapping(value = "/documents/move", consumes = { "application/json" })
	String moveFiles(@RequestBody MoveFilesDto moveFilesDto) {
		LOGGER.info("entering DocumentController moveFiles for - " + moveFilesDto.getDirectoryTo());
		String fullFolderPath = "/docs/expenseManager/filofax" + moveFilesDto.getDirectoryTo();
		documentService.moveFiles(fullFolderPath, moveFilesDto.getFileIds());
		LOGGER.info("leaving DocumentController moveFiles for - " + moveFilesDto.getDirectoryTo());
		
		return "{\"filePath\":\"" + fullFolderPath + "\"}";
	}
			

	@PostMapping(value = "/documents/upload", consumes = { "multipart/form-data" })
	DocumentDto uploadFile(@RequestPart("uploadFile") MultipartFile file, @RequestParam String type,
			@RequestParam(required = false) String path) throws Exception {
		
		LOGGER.info("entering DocumentController uploadFile");

		DocumentDto document = documentService.createDocument(path, type, file);
		
		LOGGER.info("leaving DocumentController uploadFile");
		
		return document;
	}

	@PostMapping(value = "/documents", produces = "application/json", consumes = "application/json")
	String createFile(@RequestBody DocumentDto document) throws Exception {
		
		LOGGER.info("entering DocumentController createFile - " + document.getFileName());
		
		if (!document.getOriginalFileName().equals(document.getFileName())) {
			Files.move(Paths.get(document.getFolderPath() + "/" + document.getOriginalFileName()),
					Paths.get(document.getFolderPath() + "/" + document.getFileName()));
		}
		
		LOGGER.info("leaving DocumentController createFile - " + document.getFileName());

		documentService.updateDocument(document);

		return "{\"filePath\":\"" + document.getFolderPath() + "\"}";
	}
	
	@RequestMapping(value = "/documents/{id}", method = RequestMethod.PUT, produces = "application/json", 
			consumes = "application/json", headers = "Accept=application/json")
	String updateFile(@RequestBody DocumentDto document, Long id) throws Exception {
		
		LOGGER.info("entering DocumentController updateFile - " + id);

		if (!document.getOriginalFileName().equals(document.getFileName())) {
			Files.move(Paths.get(document.getFolderPath() + "/" + document.getOriginalFileName()),
					Paths.get(document.getFolderPath() + "/" + document.getFileName()));
		}

		documentService.updateDocument(document);
		
		LOGGER.info("leaving DocumentController updateFile - " + id);

		return "{\"filePath\":\"" + document.getFolderPath() + "\"}";
	}
	
	@PostMapping(value = "/documents/directory", produces = "application/json", consumes = "application/json")
	String createDirectory(@RequestBody DocumentDto directory) throws Exception {

		LOGGER.info("entering DocumentController createDirectory - " + directory.getFileName());
		
		documentService.createDirectory(directory);
		
		LOGGER.info("leaving DocumentController createDirectory - " + directory.getFileName());

		return "{\"folderPath\":\"" + directory.getFilePath() + "\"}";
	}
	
	@RequestMapping(value = "/documents/directory", produces = "application/json", consumes = "application/json", method = RequestMethod.PUT)
	String updateDirectory(@RequestBody DocumentDto directory) throws Exception {
		LOGGER.info("entering DocumentController updateDocument - " + directory.getFileName());
		
		Files.move(Paths.get(directory.getFolderPath() + "/" + directory.getOriginalFileName()),
				Paths.get(directory.getFolderPath() + "/" + directory.getFileName()));
		
		DocumentDto newDirectory = documentService.updateDocument(directory);
		
		LOGGER.info("leaving DocumentController updateDocument - " + newDirectory.getFileName());
		
		return "{\"folderPath\":\"" + newDirectory.getFilePath() + "\"}";
	}
	
	@RequestMapping(value = "/documents/{id}", method = RequestMethod.DELETE, produces = "application/json",
			consumes = "application/json", headers = "Accept=application/json")
	String deleteDocument(@PathVariable Long id) throws Exception {
		
		LOGGER.info("entering DocumentController deleteDocument - " + id);
		
		DocumentDto document = documentService.getById(id);
		String parentFolder = document.getFolderPath();
		if (document.getIsFolder()) {
			FileUtils.deleteDirectory(new File(document.getFolderPath() + "/" + document.getFileName()));
		}
		else {
			Files.delete(Paths.get(document.getFolderPath() + "/" + document.getFileName()));
		}
		
		documentService.deleteDocument(document);
		
		LOGGER.info("leaving DocumentController deleteDocument - " + id);
		
		return "{\"folderPath\":\"" + parentFolder + "\"}";
    }

	@RequestMapping(value = "/documents/get/{type}/{id}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getFile(@PathVariable Long id, @PathVariable String type) throws Exception {
		LOGGER.info("entering DocumentController getFile - " + id);

		Path path = Paths.get(getPath(id, type));
		String mediaType = getContentType(path.getFileName().toString());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(mediaType));
		String filename = "output"
				+ path.getFileName().toString().substring(path.getFileName().toString().lastIndexOf("."));
		headers.setContentDispositionFormData(filename, filename);
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
		
		LOGGER.info("leaving DocumentController getFile - " + id);

		return new ResponseEntity<byte[]>(Files.readAllBytes(path), headers, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/documents/get/{id}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getFileById(@PathVariable Long id) throws Exception {
		
		LOGGER.info("enterting DocumentController getFileById - " + id);
		
		DocumentDto document = documentService.getById(id);
		
		HttpHeaders headers = new HttpHeaders();
		String mediaType = getContentType(document.getFileName());
		headers.setContentType(MediaType.parseMediaType(mediaType));
		String filename = "output.pdf";
		headers.setContentDispositionFormData(filename, filename);
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
		
		LOGGER.info("leaving DocumentController getFileById - " + id);
	
		return new ResponseEntity<byte[]>(Files.readAllBytes(Paths.get(document.getFilePath())), headers, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/documents/list", method = RequestMethod.POST)
	public List<DocumentDto> getFiles(@RequestBody String folder) throws Exception {
		LOGGER.info("entering DocumentController getFiles - " + folder);		
		List<DocumentDto> documents = documentService.getAll(folder);

		Collections.sort(documents);

		LOGGER.info("leaving DocumentController getFiles - " + folder);			

		return documents;
	}

	private String getContentType(String path) {
		String mediaType = "application/pdf";
		if (path.endsWith("doc") || path.endsWith("docx")) {
			mediaType = "application/msword";
		}
		if (path.endsWith("jpg") || path.endsWith("jpeg")) {
			mediaType = "image/jpeg";
		} else if (path.endsWith("xls") || path.endsWith("xlsx")) {
			mediaType = "application/vnd.ms-excel";
		}
		return mediaType;
	}

	private String getPath(Long id, String type) throws Exception {
		if (type.equals("donations")) {
			DonationDto donation = donationService.getById(id);

			return donation.getDocumentDto().getFilePath();
		} else if (type.equals("expenses")) {
			ExpenseDto expense = expenseService.getById(id);

			return expense.getDocumentDto().getFilePath();
		} else if (type.equals("incomes")) {
			IncomeDto income = incomeService.getById(id);

			return income.getDocumentDto().getFilePath();
		}

		return null;
	}

}
